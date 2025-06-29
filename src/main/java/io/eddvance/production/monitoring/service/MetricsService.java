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

    /**
     * ANCIENNE MÉTHODE MODIFIÉE
     * Enregistre une requête de taux et mesure le temps de réponse
     */
    public void recordRateRequest(String rateType, Double value) {
        try {
            if ("carbon".equals(rateType)) {
                // Utilise la nouvelle méthode qui fait tout en une fois
                metrics.incrementCarbonRateRequests();
                metrics.setLastCarbonRate(value);
            } else if ("green".equals(rateType)) {
                metrics.incrementGreenRateRequests();
                metrics.setLastGreenRate(value);
            }
        } catch (Exception e) {
            // Utilise le nouveau compteur d'erreurs
            metrics.incrementErrors("rate_calculation_error");
            throw e;
        }
    }

    /**
     * NOUVELLE MÉTHODE avec mesure du temps de réponse
     * Utilise cette méthode si tu veux mesurer les performances
     */
    public Mono<Double> recordRateRequestWithTiming(String rateType, Mono<Double> rateCalculation) {
        Instant start = Instant.now();

        return rateCalculation
                .doOnSuccess(value -> {
                    Duration responseTime = Duration.between(start, Instant.now());

                    if ("carbon".equals(rateType)) {
                        // Utilise la méthode utilitaire qui fait tout
                        metrics.recordCarbonRateRequest(value, responseTime);
                    } else if ("green".equals(rateType)) {
                        metrics.recordGreenRateRequest(value, responseTime);
                    }
                })
                .doOnError(error -> {
                    metrics.incrementErrors("rate_calculation_error");
                });
    }

    /**
     * NOUVELLE MÉTHODE pour gérer les services actifs
     */
    public void registerActiveService() {
        metrics.incrementActiveServices();
    }

    public void unregisterActiveService() {
        metrics.decrementActiveServices();
    }

    /**
     * ANCIENNE MÉTHODE GARDÉE IDENTIQUE
     */
    public Mono<String> checkServiceHealth(String serviceName) {
        try {
            // Simulation simple de vérification de santé
            return Mono.just("UP")
                    .doOnSuccess(status -> {
                        // On peut maintenant tracker les health checks
                        if ("UP".equals(status)) {
                            registerActiveService();
                        }
                    });
        } catch (Exception e) {
            metrics.incrementErrors("health_check_error");
            return Mono.just("DOWN");
        }
    }

    /**
     * NOUVELLES MÉTHODES pour obtenir les valeurs actuelles
     */
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