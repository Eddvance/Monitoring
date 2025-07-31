package io.eddvance.production.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfigurationClient {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
