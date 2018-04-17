package org.helloworld.kafka.bus;

import static org.helloworld.kafka.support.Utils.sleepFor;

import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.helloworld.kafka.service.EventTracker;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelloWorldKafkaProducer {

    @Inject
    private EventTracker eventTracker;

    public void start() {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", HelloWorldKafka.BOOTSTRAP_SERVERS);
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            Producer<String, String> producer = new KafkaProducer<>(props);
            while (true) {
                sleepFor(1000);
                UUID uuid = UUID.randomUUID();
                eventTracker.sent(uuid);
                producer.send(new ProducerRecord<>(HelloWorldKafka.TOPIC, uuid.toString(), uuid.toString()),
                        (metadata, exception) -> {
                            if (exception != null) {
                                log.error("Error detected while sending key:{}, partition:{}, offset:{}",
                                        uuid.toString(), metadata != null ? metadata.partition() : "?",
                                        metadata != null ? metadata.offset() : "?", exception);
                                eventTracker.sentNoAck(uuid, exception.getMessage());
                                return;
                            }
                            eventTracker.sentAck(uuid, metadata.offset(), metadata.timestamp(), metadata.partition());
                        });
            }
        }).start();
    }

}
