package io.rapidw.mqttrpc.driver.sample;

import io.rapidw.mqttrpc.driver.spec.Canteen;

import java.util.concurrent.CompletableFuture;

public class CanteenSample implements Canteen {
    @Override
    public CompletableFuture<String> hello(String message) {
        return CompletableFuture.completedFuture("canteen " + message);
    }
}
