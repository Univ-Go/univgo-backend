package com.univgo.backend;

import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) Application::applyInstitutionTimeZone);
        app.run(args);
    }

    /**
     * Blocks, check-in windows and every TIMESTAMP column are wall-clock times at the institution,
     * so the JVM runs on its zone (`univgo.timezone`) instead of the host's. Render runs in UTC:
     * without this a student booking at 9:21 in Bogotá was judged at 14:21 and found the 10:00,
     * 12:00 and 14:00 blocks already closed. Applied once the config is read and before any bean
     * exists, so every `now()` and the {@code Clock} bean see it. {@link ZoneId#of} fails fast on a
     * typo, where {@code TimeZone.getTimeZone(String)} would silently fall back to GMT.
     */
    private static void applyInstitutionTimeZone(ApplicationEnvironmentPreparedEvent event) {
        String zone = event.getEnvironment().getRequiredProperty("univgo.timezone");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(zone)));
    }
}
