package io.rapidw.mqttrpc.driver.sample;

import io.rapidw.mqttrpc.driver.spec.Driver;

import java.util.Arrays;
import java.util.List;

public class DriverSample implements Driver {
    @Override
    public List<Type> getTypes() {
        return Arrays.asList(Type.CANTEEN, Type.ENERGY);
    }
}
