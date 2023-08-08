package io.rapidw.mqttrpc.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    @Value("#{systemProperties['bootstrapPath']}")
    private String bootstrapPath;
}
