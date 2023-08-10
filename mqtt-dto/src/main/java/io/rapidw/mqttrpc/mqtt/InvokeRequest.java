package io.rapidw.mqttrpc.mqtt;

import io.rapidw.mqttrpc.driver.spec.Driver;
import lombok.Data;

import java.util.List;

@Data
public class InvokeRequest {
    private String clientId;
    private Long invokeId;
    private Driver.Type type;
    private String method;
    private List<Object> params;
}