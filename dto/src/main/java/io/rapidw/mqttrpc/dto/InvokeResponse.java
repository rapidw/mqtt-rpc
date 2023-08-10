package io.rapidw.mqttrpc.dto;

import lombok.Data;

@Data
public class InvokeResponse {
    private String clientId;
    private Long invokeId;
    private Object result;
}
