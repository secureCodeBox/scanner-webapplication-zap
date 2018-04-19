package io.securecodebox.zap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@ComponentScan(basePackages = {"de.otto.edison", "io.securecodebox.zap"})
@PropertySource("version.properties")  // Add a additional property file
@EnableAutoConfiguration
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
