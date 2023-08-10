package io.rapidw.mqttrpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app")
public class AppConfig {
    private Mqtt mqtt;

    @Data
    public static class Mqtt {
        private String host;
        private int port;
        private String clientId;
        private String registerRequestTopic;
        private String registerResponseTopic;
        private String heartbeatTopic;

        private String invokeRequestTopic;
        private String invokeResponseTopic;
    }
}
