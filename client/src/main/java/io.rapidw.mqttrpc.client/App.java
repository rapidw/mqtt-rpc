package io.rapidw.mqttrpc.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class App {
    private static ConfigurableApplicationContext context;
    private static AtomicInteger counter = new AtomicInteger(0);

    @SneakyThrows
    public static void main(String [] args) {
        context = SpringApplication.run(App.class, args);
    }

    private static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(App.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }
}
