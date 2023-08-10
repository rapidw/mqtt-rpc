package io.rapidw.mqttrpc.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rapidw.mqttrpc.driver.spec.Driver;
import io.rapidw.mqttrpc.dto.http.HttpInvokeRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class DriverService {
    private final Map<Class<?>, List<String>> supportedMethods = new HashMap<>();
    private final String host;
    private final int port;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DriverService(String host, int port) {
        this.host = host;
        this.port = port;

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("io.rapidw.mqttrpc.driver.spec")
                .setScanners(Scanners.SubTypes.filterResultsBy(new FilterBuilder()))
                .filterInputsBy(new FilterBuilder().includePackage("io.rapidw.mqttrpc.driver.spec")));
        val supportedTypes = reflections.getSubTypesOf(Object.class)
                .stream().filter(v -> !v.getName().matches(".*Driver$"))
                .collect(Collectors.toSet());
        supportedTypes.forEach(v -> {
            val methods = Arrays.stream(v.getMethods())
                    .map(Method::getName)
                    .filter(name -> Arrays.asList("equals", "hashCode", "toString", "notify", "notifyAl", "wait", "getClass", "clone", "finalize").contains(name))
                    .collect(Collectors.toList());
            supportedMethods.put(v, methods);
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> clazz) {

        if (!supportedMethods.containsKey(clazz)) {
            throw new RuntimeException("unsupported type");
        }

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // call server, register callback and return completable future

                if (!supportedMethods.get(clazz).contains(method.getName())) {
                    throw new RuntimeException("unsupported method");
                }

                val invoke = new HttpInvokeRequest();
                invoke.setClientId("test");
                invoke.setType(Driver.Type.valueOf(clazz.getSimpleName()));
                invoke.setMethod(method.getName());
                invoke.setParams(Arrays.asList(args));
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                final Request request = new Request.Builder()
                        .url("http://" + host + ":" + port + "/invoke")
                        .post(RequestBody.create(objectMapper.writeValueAsString(invoke), MediaType.parse("application/json")))
                        .build();
                okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, java.io.IOException e) {
                        log.error("call server error", e);
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                        if (response.body() != null) {
                            completableFuture.complete(objectMapper.readValue(response.body().string(), method.getReturnType()));
                        } else {
                            log.error("empty response");
                        }
                    }
                });
                return completableFuture;
            }
        });
    }
}
