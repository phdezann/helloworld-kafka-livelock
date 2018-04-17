package org.helloworld.kafka.service;

import java.util.Properties;

import org.apache.kafka.common.security.JaasUtils;
import org.helloworld.kafka.bus.HelloWorldKafka;
import org.springframework.cloud.stream.binder.kafka.admin.Kafka10AdminUtilsOperation;
import org.springframework.stereotype.Service;

import kafka.utils.ZkUtils;

@Service
public class KafkaTopicProvisioner {

    public void createTopic() {
        final ZkUtils zkUtils = ZkUtils
                .apply(HelloWorldKafka.ZK_SERVERS, 10000, 10000, JaasUtils.isZkSecurityEnabled());
        new Kafka10AdminUtilsOperation().invokeCreateTopic(zkUtils, HelloWorldKafka.TOPIC, 3, 3, new Properties());
    }

}
