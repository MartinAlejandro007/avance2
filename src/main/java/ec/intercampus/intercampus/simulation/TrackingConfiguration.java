package ec.intercampus.intercampus.simulation;

import org.springframework.context.annotation.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(TrackerSettings.class)
public class TrackingConfiguration {
    @Bean
    public Clock trackingClock() { return Clock.systemUTC(); }
}
