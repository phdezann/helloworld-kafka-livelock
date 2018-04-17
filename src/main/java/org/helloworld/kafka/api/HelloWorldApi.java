package org.helloworld.kafka.api;

import javax.inject.Inject;

import org.helloworld.kafka.bus.HelloWorldKafkaListener;
import org.helloworld.kafka.bus.HelloWorldKafkaProducer;
import org.helloworld.kafka.service.EventTracker;
import org.helloworld.kafka.service.KafkaTopicProvisioner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldApi {

    @Inject
    private KafkaTopicProvisioner kafkaTopicProvisioner;

    @Inject
    private HelloWorldKafkaListener helloWorldKafkaListener;

    @Inject
    private HelloWorldKafkaProducer helloWorldKafkaProducer;

    @Inject
    private EventTracker eventTracker;

    @GetMapping("/kafka/topic/create")
    public String createTopic() {
        kafkaTopicProvisioner.createTopic();
        return "Topic created \n";
    }

    @GetMapping("/kafka/producer/start")
    public String startProducer() {
        helloWorldKafkaProducer.start();
        return "Producer started \n";
    }

    @GetMapping("/kafka/consumer/start")
    public String startConsumer() {
        helloWorldKafkaListener.start();
        return "Consumer started \n";
    }

    @GetMapping("/event-tracker/clear")
    public String clearEventTracker() {
        eventTracker.clear();
        return "EventTracker cleared \n";
    }
}
