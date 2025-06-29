package io.eddvance.production.monitoring.config;

import io.eddvance.production.monitoring.metrics.LowCarbMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public LowCarbMetrics lowCarbMetrics(MeterRegistry registry) {
        return new LowCarbMetrics(registry);
    }
}