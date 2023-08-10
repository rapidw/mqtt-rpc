package io.rapidw.mqttrpc.driver.spec;

import java.util.List;

public interface Driver {
    List<Type> getTypes();

    enum Type {
        CANTEEN,
        ENERGY,
        ;
    }
}
