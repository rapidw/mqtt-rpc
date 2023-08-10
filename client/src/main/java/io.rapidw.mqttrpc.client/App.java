package io.rapidw.mqttrpc.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@EnableScheduling
public class App {

    public static void main(String [] args) {
        SpringApplication.run(App.class, args);
    }
}
