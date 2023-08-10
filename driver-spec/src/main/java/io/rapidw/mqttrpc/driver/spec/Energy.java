package io.rapidw.mqttrpc.driver.spec;

import java.util.concurrent.CompletableFuture;

public interface Energy {
    default Driver.Type getType() {
        return Driver.Type.ENERGY;
    }

    CompletableFuture<String> hello(String name);
}
