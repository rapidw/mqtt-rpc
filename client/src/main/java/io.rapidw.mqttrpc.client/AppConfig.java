package io.rapidw.mqttrpc.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    @Value("#{systemProperties['bootstrapPath']}")
    private String bootstrapPath;
    private String id;
    private Mqtt mqtt;

    @Data
    public static class Mqtt {
        private String clientId;
        private String host;
        private int port;
        private String registerRequestTopic;
        private String registerResponseTopic;
        private String heartbeatTopic;

        private String invokeRequestTopic;
        private String invokeResponseTopic;
    }
}
