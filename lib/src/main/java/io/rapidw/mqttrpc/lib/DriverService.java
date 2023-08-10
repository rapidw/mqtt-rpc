package io.rapidw.mqttrpc.lib;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DriverService {
    private final Set<Class<?>> supportedTypes;
    private String host;
    private int port;

    public DriverService(String host, int port) {
        this.host = host;
        this.port = port;

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("io.rapidw.mqttrpc.driver.spec")
                .setScanners(Scanners.SubTypes.filterResultsBy(new FilterBuilder()))
                .filterInputsBy(new FilterBuilder().includePackage("io.rapidw.mqttrpc.driver.spec")));
        supportedTypes = reflections.getSubTypesOf(Object.class)
                .stream().filter(v -> !v.getName().matches(".*Driver$"))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> clazz) {

        if (!supportedTypes.contains(clazz)) {
            throw new RuntimeException("unsupported type");
        }

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // call server, register callback and return completable future
                return new CompletableFuture<>();
            }
        });
    }
}
