package io.rapidw.mqttrpc.bootstrap;


import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.UpdateOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;

public class Bootstrap {

    public static void main(String[] args) {
        System.out.println("Bootstrap Staring...");
        Configuration config;
        try (InputStream in = new URL("http://127.0.0.1:8000/out.xml").openStream()) {
            config = Configuration.read(new InputStreamReader(in));
            if (config.requiresUpdate()) {
                System.out.println("Update Starting...");
                config.update(UpdateOptions.archive(Path.of("update.zip")));
                Archive.read("update.zip").install();
                System.out.println("Update finished");
            }
            System.out.println("Application Starting...");
            config.launch();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
