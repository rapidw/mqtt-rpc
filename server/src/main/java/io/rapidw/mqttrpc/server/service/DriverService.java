package io.rapidw.mqttrpc.server.service;

import io.rapidw.mqttrpc.driver.spec.Driver;
import io.rapidw.mqttrpc.dto.InvokeRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final MqttService mqttService;

    private Map<Long, InvokeContext> pendingInvokes = new ConcurrentHashMap<>();

    public void invoke() {
        val request = new InvokeRequest();
        request.setInvokeId(1L);
        request.setType(Driver.Type.CANTEEN);
        request.setMethod("hello");
        request.setParams(List.of("world"));
        mqttService.publishInvoke(request);
    }

    private static class InvokeContext {
        private InvokeRequest request;
        private long timeout;
    }
}
