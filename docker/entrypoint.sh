#!/bin/bash

# Default ENV variables
export KAFKA_LOG_DIRS=${KAFKA_LOG_DIRS:-"/data"}

# Start kafka
./bin/kafka-server-start.sh config/server.properties --override advertised.listeners=${KAFKA_ADVERTISED_LISTENERS} --override broker.id=${KAFKA_BROKER_ID} --override zookeeper.connect=${KAFKA_ZOOKEEPER_CONNECT}
