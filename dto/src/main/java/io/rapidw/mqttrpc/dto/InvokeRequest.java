package io.rapidw.mqttrpc.dto;

import io.rapidw.mqttrpc.driver.spec.Driver;
import lombok.Data;

import java.util.List;

@Data
public class InvokeRequest {
    private String clintId;
    private Long invokeId;
    private Driver.Type type;
    private String method;
    private List<Object> params;
}