package org.helloworld.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringCloudApplication
@EnableScheduling
public class HelloWorldApplication {

    public static void main(String... args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

}
