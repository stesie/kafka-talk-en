package de.brokenpipe.kafkatalk.streamsdemo.logparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication //(scanBasePackages = "de.brokenpipe.kafkatalk.streamsdemo")
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }


}
