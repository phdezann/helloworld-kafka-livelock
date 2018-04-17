package org.helloworld.kafka.bus;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.helloworld.kafka.service.EventTracker;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelloWorldKafkaListener {

    @Inject
    private EventTracker eventTracker;

    public void start() {
        createConsumerInDedicatedThread("kafka-consumer-1");
        createConsumerInDedicatedThread("kafka-consumer-2");
        createConsumerInDedicatedThread("kafka-consumer-3");
    }

    private void createConsumerInDedicatedThread(String threadName) {
        Thread thread = new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", HelloWorldKafka.BOOTSTRAP_SERVERS);
            props.put("group.id", "helloWorldGroup");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(HelloWorldKafka.TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    eventTracker.consumed(UUID.fromString(record.value()), Thread.currentThread().getName());
                }
            }
        });
        thread.setName(threadName);
        thread.start();
    }

}
