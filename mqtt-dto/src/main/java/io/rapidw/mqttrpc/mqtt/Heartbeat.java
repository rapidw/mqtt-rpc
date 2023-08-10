package io.rapidw.mqttrpc.mqtt;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Heartbeat {
    private String clientId;
}
