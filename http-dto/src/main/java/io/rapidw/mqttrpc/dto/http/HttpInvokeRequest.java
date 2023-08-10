package io.rapidw.mqttrpc.dto.http;

import io.rapidw.mqttrpc.driver.spec.Driver;
import lombok.Data;

import java.util.List;

@Data
public class HttpInvokeRequest {
    private String clientId;
    private Driver.Type type;
    private String method;
    private List<Object> params;
}
