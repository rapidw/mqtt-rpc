package io.rapidw.mqttrpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rapidw.mqttrpc.driver.spec.Canteen;
import io.rapidw.mqttrpc.driver.spec.Driver;
import io.rapidw.mqttrpc.driver.spec.Energy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverService {

    private Canteen canteen;
    private Energy energy;

    private static final Map<Driver.Type, Map<String, Method>> map = new HashMap<>();
    private final ObjectMapper objectMapper;

    static {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("io.rapidw.mqttrpc.driver.spec")
                .setScanners(Scanners.SubTypes.filterResultsBy(new FilterBuilder()))
                .filterInputsBy(new FilterBuilder().includePackage("io.rapidw.mqttrpc.driver.spec")));
        Set<Class<?>> types = reflections.getSubTypesOf(Object.class)
                .stream().filter(v -> !v.getName().matches(".*Driver$"))
                .collect(Collectors.toSet());
        types.forEach(v -> {
            val methodMap = new HashMap<String, Method>();
            for (Method method : v.getMethods()) {
                methodMap.put(method.getName(), method);
            }
            map.put(Driver.Type.valueOf(v.getSimpleName().toUpperCase()), methodMap);
        });
    }

    @SneakyThrows
    public List<Driver.Type> getAllDriverType() {
        log.info("current working dir {}", System.getProperty("user.dir"));
        File folder = new File(System.getProperty("user.dir"));
        File[] files = folder.listFiles((file, name) -> name.endsWith(".jar"));

        val classloader = new JarStreamClassLoader(getClass().getClassLoader());
        if (files != null) {
            for (File file : files) {
                val jarInputStream = new JarInputStream(new FileInputStream(file));
                classloader.addJar(jarInputStream);
            }
            ServiceLoader<Driver> demoDriverSpecs = ServiceLoader.load(Driver.class, classloader);
            AtomicReference<List<Driver.Type>> types = new AtomicReference<>();
            demoDriverSpecs.findFirst().ifPresent(v -> {
                for (Driver.Type type : v.getTypes()) {
                    switch (type) {
                        case CANTEEN -> canteen = ServiceLoader.load(Canteen.class, classloader).findFirst().get();
                        case ENERGY -> energy = ServiceLoader.load(Energy.class, classloader).findFirst().get();
                    }
                }
                types.set(v.getTypes());
            });
            return types.get();
        }
        return List.of();
    }

    @SuppressWarnings("rawtypes")
    public Object invoke(Driver.Type type, String method, List<Object> args) {
        if (map.containsKey(type)) {
            val methodMap = map.get(type);
            if (methodMap.containsKey(method)) {
                val methodObj = methodMap.get(method);
                try {
                    // prepare args
                    val params = methodObj.getParameters();
                    if (params.length != args.size()) {
                        throw new RuntimeException("args size not match");
                    }
                    for (int i = 0; i < params.length; i++) {
                        val json = objectMapper.writeValueAsString(args.get(i));
                        val clazz = methodObj.getParameterTypes()[i];
                        args.set(i, objectMapper.readValue(json, clazz));
                    }

                    val invoked = ((CompletableFuture) methodObj.invoke(type == Driver.Type.CANTEEN ? canteen : energy, args.toArray()));
                    log.info("invoke finish {}", invoked.get());
                    return invoked.get();
                } catch (Exception e) {
                    log.error("invoke error", e);
                }
            }
        }
        throw new RuntimeException("method not found");
    }


}
