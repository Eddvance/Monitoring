package io.eddvance.production.monitoring.service;

import io.eddvance.production.monitoring.model.ServiceEndpoint;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class HealthMonitorService {

    private final WebClient webClient;
    private final MeterRegistry meterRegistry;
    private final Map<String, Integer> serviceStatus = new ConcurrentHashMap<>();
    private final List<ServiceEndpoint> services = new ArrayList<>();

    public HealthMonitorService(WebClient.Builder webClientBuilder, MeterRegistry meterRegistry) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.meterRegistry = meterRegistry;
        initializeServices();
        createServiceGauges();
    }

    private void initializeServices() {
        services.add(new ServiceEndpoint("Lowcarb-app", "http://host.docker.internal:8080/actuator/health"));
        services.add(new ServiceEndpoint("CoalFired-service", "http://host.docker.internal:3000/actuator/health"));
        services.add(new ServiceEndpoint("LowCarbPower-service", "http://host.docker.internal:8081/actuator/health"));

    }

    private void createServiceGauges() {
        for (ServiceEndpoint service : services) {
            Gauge.builder("service.health.status", serviceStatus,
                            map -> map.getOrDefault(service.getName(), 0))
                    .tag("service", service.getName())
                    .description("Health status of " + service.getName() + " (1=UP, 0=DOWN)")
                    .register(meterRegistry);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void checkServicesHealth() {
        for (ServiceEndpoint service : services) {
            checkServiceHealth(service)
                    .subscribe();
        }
    }

    private Mono<Void> checkServiceHealth(ServiceEndpoint service) {
        return webClient.get()
                .uri(service.getUrl())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> {
                    String status = (String) response.get("status");

                    if ("UP".equalsIgnoreCase(status)) {
                        serviceStatus.put(service.getName(), 1);
                        meterRegistry.counter("service.health.check",
                                "service", service.getName(),
                                "status", "success").increment();
                    } else {
                        serviceStatus.put(service.getName(), 0);
                        meterRegistry.counter("service.health.check",
                                "service", service.getName(),
                                "status", "down").increment();
                    }
                })
                .doOnError(error -> {
                    serviceStatus.put(service.getName(), 0);
                    meterRegistry.counter("service.health.check",
                            "service", service.getName(),
                            "status", "error").increment();


                    meterRegistry.counter("service.health.errors",
                            "service", service.getName(),
                            "error", error.getClass().getSimpleName()).increment();
                })
                .onErrorResume(error -> Mono.empty())
                .then();
    }
}