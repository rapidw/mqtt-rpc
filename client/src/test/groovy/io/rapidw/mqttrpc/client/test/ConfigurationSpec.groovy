package io.rapidw.mqttrpc.client.test

import org.update4j.Configuration
import org.update4j.FileMetadata
import spock.lang.Specification

//@Ignore
class ConfigurationSpec extends Specification {

    def gen() {
        given:
        Configuration config = Configuration.builder()
                .baseUri("http://localhost:8000")
                .basePath('${user.home}/myapp/')
                .files(FileMetadata.streamDirectory("build/libs/")
                        .peek(r -> r.classpath(r.getSource().toString().endsWith(".jar"))))
                .property("default.launcher.main.class", "org.springframework.boot.loader.JarLauncher")
                .property("default.launcher.system.bootstrapPath", "ffff")
                .build()

        try (def writer = new FileWriter('ggg/out.xml')) {
            config.write(writer)
        }
    }

//    def sync() {
//        given:
//        Configuration config = Configuration.read(new FileReader('ggg/out.xml'))
//        config.sync()
//    }
}