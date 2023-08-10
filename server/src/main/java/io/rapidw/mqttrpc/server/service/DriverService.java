package io.rapidw.mqttrpc.server.service;

import io.rapidw.mqttrpc.dto.http.HttpInvokeRequest;
import io.rapidw.mqttrpc.mqtt.InvokeRequest;
import io.rapidw.mqttrpc.server.config.AppConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final AppConfig config;
    private final MqttService mqttService;

    private final AtomicLong invokeIdGenerator = new AtomicLong(0);

    private final Map<String, Map<Long, InvokeContext>> pendingInvokes = new ConcurrentHashMap<>();

    public Object invoke(HttpInvokeRequest httpInvokeRequest) {
        val request = new InvokeRequest();
        Long invokeId = nextInvokeId();
        request.setInvokeId(invokeId);
        request.setType(httpInvokeRequest.getType());
        request.setMethod(httpInvokeRequest.getMethod());
        request.setParams(httpInvokeRequest.getParams());
        val context = new InvokeContext();
        context.setInvokeId(invokeId);
        context.setClientId(httpInvokeRequest.getClientId());
        mqttService.publishInvoke(request);
        val invokes =  pendingInvokes.computeIfAbsent(context.clientId, (key) -> new ConcurrentHashMap<>());
        invokes.put(invokeId, context);

        // hold this thread until the response is received
        try {
            context.getLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return context.getResponse();
    }

    @Data
    private static class InvokeContext {
        private CountDownLatch latch = new CountDownLatch(1);
        private String clientId;
        private Long invokeId;
        private Object response;
    }

    private Long nextInvokeId() {
        return invokeIdGenerator.accumulateAndGet(1, (current, update) -> (current += update) == Long.MAX_VALUE ? Long.MIN_VALUE : current);
    }
}
