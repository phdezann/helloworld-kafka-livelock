helloworld-kafka
==================

Start this script to reproduce the issue:
```
$ ./spin.sh
```

At some point, you should see `livelock detected`, checkout the logs to investigate further.

You should see that all 3 consumers are not consuming anything. They are doing this :

```
"kafka-consumer-1@10574" daemon prio=5 tid=0x31 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	 blocks kafka-coordinator-heartbeat-thread | addressRepository@10570
	  at sun.nio.ch.EPollArrayWrapper.epollWait(EPollArrayWrapper.java:-1)
	  at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	  at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:93)
	  at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
	  - locked <0x2978> (a sun.nio.ch.EPollSelectorImpl)
	  - locked <0x2979> (a java.util.Collections$UnmodifiableSet)
	  - locked <0x297a> (a sun.nio.ch.Util$3)
	  at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
	  at org.apache.kafka.common.network.Selector.select(Selector.java:470)
	  at org.apache.kafka.common.network.Selector.poll(Selector.java:286)
	  at org.apache.kafka.clients.NetworkClient.poll(NetworkClient.java:260)
	  at org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient.poll(ConsumerNetworkClient.java:232)
	  - locked <0x297b> (a org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient)
	  at org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient.poll(ConsumerNetworkClient.java:209)
	  at org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient.awaitMetadataUpdate(ConsumerNetworkClient.java:148)
	  at org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient.awaitMetadataUpdate(ConsumerNetworkClient.java:136)
	  at org.apache.kafka.clients.consumer.internals.AbstractCoordinator.ensureCoordinatorReady(AbstractCoordinator.java:197)
	  - locked <0x296e> (a org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
	  at org.apache.kafka.clients.consumer.internals.ConsumerCoordinator.poll(ConsumerCoordinator.java:248)
	  at org.apache.kafka.clients.consumer.KafkaConsumer.pollOnce(KafkaConsumer.java:1013)
	  at org.apache.kafka.clients.consumer.KafkaConsumer.poll(KafkaConsumer.java:979)
	  at org.helloworld.kafka.bus.HelloWorldKakfaListener.lambda$createConsumerInDedicatedThread$0(HelloWorldKakfaListener.java:52)
	  at org.helloworld.kafka.bus.HelloWorldKakfaListener$$Lambda$9.1298683140.run(Unknown Source:-1)
	  at java.lang.Thread.run(Thread.java:748)
```

And `<0x296e>` actually block other threads `kafka-coordinator-heartbeat-thread`:

```
"kafka-coordinator-heartbeat-thread | addressRepository@10569" daemon prio=5 tid=0x36 nid=NA waiting for monitor entry
  java.lang.Thread.State: BLOCKED
	 waiting for kafka-consumer-2@10573 to release lock on <0x296d> (a org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
	  at java.lang.Object.wait(Object.java:-1)
	  at org.apache.kafka.clients.consumer.internals.AbstractCoordinator$HeartbeatThread.run(AbstractCoordinator.java:872)
```

This looks like a livelock.

Miscellaneous Kafka commands:
```
kafka-topics.sh --topic my-topic --describe --zookeeper helloworld-zookeeper
kafka-consumer-groups.sh --describe --group helloWorldGroup --verbose --bootstrap-server helloworld-kafka-1:9092

```
