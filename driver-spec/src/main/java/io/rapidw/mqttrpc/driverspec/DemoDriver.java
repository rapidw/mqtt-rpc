package io.rapidw.mqttrpc.driverspec;

import java.util.concurrent.CompletableFuture;

public interface DemoDriver {
    CompletableFuture<String> hello(String message);
}
