package io.rapidw.mqttrpc.mqtt;

import lombok.Data;

@Data
public class InvokeResponse {
    private String clientId;
    private Long invokeId;
    private Object result;
}
