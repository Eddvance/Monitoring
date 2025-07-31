package io.eddvance.production.monitoring.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
public class MetricsController {

    private final Counter requestCounter;
    private final Timer requestTimer;
    private final MeterRegistry meterRegistry;


    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("custom.requests.total")
                .description("Total number of requests")
                .tag("type", "api")
                .register(meterRegistry);

        this.requestTimer = Timer.builder("custom.request.duration")
                .description("Request processing time")
                .tag("type", "api")
                .register(meterRegistry);
    }

    @GetMapping("/health-check")
    @Timed(value = "custom.endpoint.timing", description = "Time taken to execute health check")
    public String healthCheck() {
        requestCounter.increment();

        return requestTimer.record(() -> {
            // Simuler un traitement
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Service is healthy";
        });
    }

    @PostMapping("/event")
    @Timed(value = "custom.endpoint.timing", description = "Time taken to process event")
    public String logEvent(@RequestBody String event) {
        // Compteur pour différents types d'événements
        meterRegistry.counter("custom.events", "type", event).increment();
        return "Event logged: " + event;
    }

    @GetMapping("/gauge-demo")
    public String gaugeDemo() {
        // Exemple de gauge (métrique qui peut monter et descendre)
        meterRegistry.gauge("custom.active.sessions", Math.random() * 100);
        return "Gauge updated";
    }
}