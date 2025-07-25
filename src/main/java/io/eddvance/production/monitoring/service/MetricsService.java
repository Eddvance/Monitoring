package io.eddvance.production.monitoring.service;

import io.eddvance.production.monitoring.metrics.LowCarbMetrics;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class MetricsService {

    private final LowCarbMetrics metrics;

    public MetricsService(LowCarbMetrics metrics) {
        this.metrics = metrics;
    }

    public void recordRateRequest(String rateType, Double value) {
        try {
            if ("carbon".equals(rateType)) {
                metrics.incrementCarbonRateRequests();
                metrics.setLastCarbonRate(value);
            } else if ("green".equals(rateType)) {
                metrics.incrementGreenRateRequests();
                metrics.setLastGreenRate(value);
            }
        } catch (Exception e) {
            metrics.incrementErrors("rate_calculation_error");
            throw e;
        }
    }


    public Mono<Double> recordRateRequestWithTiming(String rateType, Mono<Double> rateCalculation) {
        Instant start = Instant.now();

        return rateCalculation
                .doOnSuccess(value -> {
                    Duration responseTime = Duration.between(start, Instant.now());

                    if ("carbon".equals(rateType)) {
                        metrics.recordCarbonRateRequest(value, responseTime);
                    } else if ("green".equals(rateType)) {
                        metrics.recordGreenRateRequest(value, responseTime);
                    }
                })
                .doOnError(error -> {
                    metrics.incrementErrors("rate_calculation_error");
                });
    }

    public void registerActiveService() {
        metrics.incrementActiveServices();
    }

    public void unregisterActiveService() {
        metrics.decrementActiveServices();
    }


    public Mono<String> checkServiceHealth(String serviceName) {
        try {
            return Mono.just("UP")
                    .doOnSuccess(status -> {
                        if ("UP".equals(status)) {
                            registerActiveService();
                        }
                    });
        } catch (Exception e) {
            metrics.incrementErrors("health_check_error");
            return Mono.just("DOWN");
        }
    }


    public double getCurrentCarbonRate() {
        return metrics.getLastCarbonRate();
    }

    public double getCurrentGreenRate() {
        return metrics.getLastGreenRate();
    }

    public long getActiveServicesCount() {
        return metrics.getActiveServicesCount();
    }
}