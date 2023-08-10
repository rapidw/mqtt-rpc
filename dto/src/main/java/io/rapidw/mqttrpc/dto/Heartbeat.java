package io.rapidw.mqttrpc.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Heartbeat {
    private String clientId;
}
