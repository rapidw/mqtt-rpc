package io.rapidw.mqttrpc.driver.spec;

import java.util.concurrent.CompletableFuture;

public interface Canteen {
    default Driver.Type getType() {
        return Driver.Type.CANTEEN;
    }

    CompletableFuture<String> hello(String name);
}
