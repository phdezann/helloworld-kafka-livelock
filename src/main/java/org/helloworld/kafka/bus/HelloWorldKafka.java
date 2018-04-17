package org.helloworld.kafka.bus;

public interface HelloWorldKafka {

    String ZK_SERVERS = "helloworld-zookeeper:2181";

    String BOOTSTRAP_SERVERS = "helloworld-kafka-1:9092,helloworld-kafka-2:9092,helloworld-kafka-3:9092";

    String TOPIC = "my-topic";

}
