#!/bin/bash

set -e

export HELLOWORLD_APP=172.170.0.50
export HELLOWORLD_ZK=172.170.0.80
export HELLOWORLD_KAFKA_1=172.170.0.81
export HELLOWORLD_KAFKA_2=172.170.0.82
export HELLOWORLD_KAFKA_3=172.170.0.83

setup() {
  # stop containers (if started before)
  docker-compose down --volumes -t 0 || true

  # start containers
  docker-compose up --build -d

  # wait for application startup
  grep -q 'Started HelloWorldApplication' <(docker logs -f helloworld-app)

  # create the topic "my-topic"
  curl -X GET http://${HELLOWORLD_APP}/kafka/topic/create
  sleep 1

  # start producing messages on "my-topic"
  curl -X GET http://${HELLOWORLD_APP}/kafka/producer/start
  sleep 1

  # start consuming messages on "my-topic"
  curl -X GET http://${HELLOWORLD_APP}/kafka/consumer/start
  sleep 10

  # kill one broker
  docker rm helloworld-kafka-2 -f
  sleep 10

  # clear stats
  curl -X GET http://${HELLOWORLD_APP}/event-tracker/clear

  # detect the issue
  errorCounter=0
  result=""
  while IFS='' read -r line || [[ -n "$line" ]]; do
    if [[ ${line} = *"coordinator is not available." ]]
    then
      errorCounter=$((errorCounter+1))
      if [[ ${errorCounter} -gt 1000 ]]
      then
        echo "livelock detected"
        return
      fi
    fi
    if [[ ${line} = *"from 3 partitions) -----" ]]
    then
      echo "could not reproduce the issue this time, re-create the cluster from scratch"
      result="retry"
      return
    fi
  done < <(docker logs --tail 0 -f helloworld-app)
}

setup
while [[ ${result} = "retry" ]]; do
   setup
done
