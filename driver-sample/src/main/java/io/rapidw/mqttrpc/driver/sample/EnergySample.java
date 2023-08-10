package io.rapidw.mqttrpc.driver.sample;

import io.rapidw.mqttrpc.driver.spec.Energy;

import java.util.concurrent.CompletableFuture;

public class EnergySample implements Energy {
    public CompletableFuture<String> hello(String message) {
        return CompletableFuture.completedFuture("energy " + message);
    }
}
