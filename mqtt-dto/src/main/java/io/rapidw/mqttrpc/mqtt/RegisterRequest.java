package io.rapidw.mqttrpc.mqtt;

import io.rapidw.mqttrpc.driver.spec.Driver;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {
    private String clientId;
    private List<Driver.Type> types;
}
