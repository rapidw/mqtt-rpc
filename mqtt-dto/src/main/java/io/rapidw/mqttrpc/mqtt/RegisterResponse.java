package io.rapidw.mqttrpc.mqtt;

import lombok.Data;

@Data
public class RegisterResponse {

    public RegisterResponse() {

    }

    public RegisterResponse(String clientId, Status status) {
        this.clientId = clientId;
        this.status = status;

    }
    private String clientId;
    private Status status;


    public enum Status {
        SUCCESS,
        ERROR
    }
}
