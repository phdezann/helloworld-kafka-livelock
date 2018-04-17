package org.helloworld.kafka.support;

public class Utils {

    public static void sleepFor(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
