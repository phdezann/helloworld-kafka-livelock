package org.helloworld.kafka.service;

import static io.vavr.collection.HashSet.empty;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.helloworld.kafka.service.EventTracker.MessageStateEnum.MESSAGE_CONSUMED;
import static org.helloworld.kafka.service.EventTracker.MessageStateEnum.MESSAGE_SENT;
import static org.helloworld.kafka.service.EventTracker.MessageStateEnum.MESSAGE_SENT_ACK;
import static org.helloworld.kafka.service.EventTracker.MessageStateEnum.MESSAGE_SENT_NO_ACK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import org.helloworld.kafka.bus.HelloWorldKafkaListener;
import org.helloworld.kafka.bus.HelloWorldKafkaProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

@Service
@Slf4j
public class EventTracker {

    @Inject
    private HelloWorldKafkaProducer helloWorldKafkaProducer;

    @Inject
    private HelloWorldKafkaListener helloWorldKafkaListener;

    public enum MessageStateEnum {
        MESSAGE_SENT, MESSAGE_SENT_ACK, MESSAGE_SENT_NO_ACK, MESSAGE_CONSUMED
    }

    @Data
    @AllArgsConstructor
    @Setter
    private static class Message {
        private UUID id;
        private Option<MessageStateEnum> state;
        private Option<Long> offset;
        private Option<Long> timestamp;
        private Option<Integer> partition;
        private Option<String> consumerThread;
        private Option<String> error;
    }

    private List<Message> events = List.empty();

    public synchronized void sent(UUID id) {
        events = events.append(new Message(id, some(MESSAGE_SENT), none(), none(), none(), none(), none()));
    }

    public synchronized void sentAck(UUID id, long offset, long timestamp, int partition) {
        events = events
                .append(new Message(id, some(MESSAGE_SENT_ACK), some(offset), some(timestamp), some(partition), none(),
                        none()));
    }

    public synchronized void sentNoAck(UUID id, String error) {
        events = events.append(new Message(id, some(MESSAGE_SENT_NO_ACK), none(), none(), none(), none(), some(error)));
    }

    public synchronized void consumed(UUID id, String thread) {
        events = events.append(new Message(id, some(MESSAGE_CONSUMED), none(), none(), none(), some(thread), none()));
    }

    public void print() {
        Seq<Message> processedEvents = reorder();
        List<Message> sent = List.ofAll(processedEvents).filter(m -> m.getState().get() == MESSAGE_SENT);
        log.info("----- Start Messages Sent ({}) -----", sent.size());
        sent.forEach(message -> log.info(print(message)));
        log.info("----- End Messages Sent -----");
        List<Message> sentAck = List.ofAll(processedEvents).filter(m -> m.getState().get() == MESSAGE_SENT_ACK);
        log.info("----- Start Messages Sent Ack ({}) -----", sentAck.size());
        sentAck.forEach(message -> log.info(print(message)));
        log.info("----- End Messages Sent Ack -----");
        List<Message> sentNoAck = List.ofAll(processedEvents).filter(m -> m.getState().get() == MESSAGE_SENT_NO_ACK);
        log.info("----- Start Messages Sent No Ack ({}) -----", sentNoAck.size());
        sentNoAck.forEach(message -> log.info(print(message)));
        log.info("----- End Messages Sent No Ack -----");
        List<Message> consumed = List.ofAll(processedEvents).filter(m -> m.getState().get() == MESSAGE_CONSUMED);
        List<Message> tenLastMessages = consumed.takeRight(10);
        int partCount = tenLastMessages.filter(message -> message.getPartition().isDefined())
                .foldLeft(empty(), (acc, elt) -> acc.add(elt.getPartition().get())).size();
        log.info("----- Start Messages Consumed (last 10 of {}, from {} partitions) -----", consumed.size(), partCount);
        tenLastMessages.forEach(message -> log.info(print(message)));
        log.info("----- End Messages Consumed -----");
        log.info("");
    }

    private Seq<Message> reorder() {
        return events.sortBy(e -> e.getState().get()).reverse().foldLeft(HashMap.<UUID, Message>empty(), (acc, elt) -> {
            if (acc.containsKey(elt.getId())) {
                Message message = acc.get(elt.getId()).get();
                if (message.getState().isEmpty()) {
                    message.setState(elt.getState());
                }
                if (message.getOffset().isEmpty()) {
                    message.setOffset(elt.getOffset());
                }
                if (message.getTimestamp().isEmpty()) {
                    message.setTimestamp(elt.getTimestamp());
                }
                if (message.getPartition().isEmpty()) {
                    message.setPartition(elt.getPartition());
                }
                if (message.getConsumerThread().isEmpty()) {
                    message.setConsumerThread(elt.getConsumerThread());
                }
                if (message.getError().isEmpty()) {
                    message.setError(elt.getError());
                }
                acc = acc.put(message.getId(), message);
            } else {
                acc = acc.put(elt.getId(), elt);
            }
            return acc;
        }).values().sortBy(e -> e.getTimestamp().getOrElse(0L));
    }

    private String print(Message message) {
        switch (message.getState().get()) {
        case MESSAGE_SENT:
            return String.format("%s", message.getId().toString());
        case MESSAGE_SENT_ACK:
            return String
                    .format("%s: offset:%s partition:%s date:%s", message.getId().toString(), message.getOffset().get(),
                            message.getPartition().get(), print(new Date(message.getTimestamp().get())));
        case MESSAGE_SENT_NO_ACK:
            return String.format("%s: error:%s", message.getId().toString(), message.getError().get());
        case MESSAGE_CONSUMED:
            return String.format("%s: offset:%s partition:%s thread:%s date:%s", message.getId().toString(),
                    message.getOffset().get(), message.getPartition().get(), message.getConsumerThread().get(),
                    print(new Date(message.getTimestamp().get())));
        default:
            throw new IllegalStateException();
        }
    }

    private static String print(Date date) {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(date);
    }

    public void clear() {
        events = List.empty();
    }

    @Scheduled(fixedDelay = 5000)
    public void schedulePrint() {
        print();
    }

}
