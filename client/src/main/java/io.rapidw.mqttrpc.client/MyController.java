package io.rapidw.mqttrpc.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MyController {
    private final AppConfig appConfig;

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

    @RequestMapping("/hello2")
    public String hello2() {
        return "hello2";
    }

    @RequestMapping("/shutdown")
    public String shutdown() {
//        new ProcessBuilder("java", "-jar", )
        System.exit(0);
        return "ok";
    }

    @RequestMapping("/getConfig")
    public String getConfig() {
        log.info(System.getProperty("bootstrapPath"));
        return appConfig.getBootstrapPath();
    }
}
