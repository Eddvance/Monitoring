package io.eddvance.production.monitoring.controller;

import io.eddvance.production.monitoring.model.ServiceMetric;
import io.eddvance.production.monitoring.service.MetricsService;
import io.eddvance.production.monitoring.service.MonitoringService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final MetricsService metricsService; // AJOUT

    public MonitoringController(MonitoringService monitoringService, MetricsService metricsService) {
        this.monitoringService = monitoringService;
        this.metricsService = metricsService; // AJOUT
    }

    /**
     * ENDPOINT EXISTANT - fonctionne toujours pareil
     */
    @PostMapping("/metric")
    public Mono<ServiceMetric> recordMetric(
            @RequestParam String serviceName,
            @RequestParam String metricName,
            @RequestParam Double value,
            @RequestParam String status) {
        return monitoringService.saveMetric(serviceName, metricName, value, status);
    }

    /**
     * NOUVEL ENDPOINT - Pour les taux avec mesure automatique des performances
     */
    @PostMapping("/rate")
    public Mono<String> recordRate(
            @RequestParam String rateType, // "carbon" ou "green"
            @RequestParam Double value) {

        // Utilise la nouvelle méthode avec timing automatique
        return metricsService.recordRateRequestWithTiming(rateType, Mono.just(value))
                .map(recordedValue -> "Taux " + rateType + " enregistré: " + recordedValue);
    }

    /**
     * NOUVEL ENDPOINT - Pour obtenir les valeurs actuelles
     */
    @GetMapping("/current-rates")
    public Mono<String> getCurrentRates() {
        return Mono.fromCallable(() -> {
            double carbonRate = metricsService.getCurrentCarbonRate();
            double greenRate = metricsService.getCurrentGreenRate();
            long activeServices = metricsService.getActiveServicesCount();

            return String.format(
                    "Taux carbone actuel: %.2f, Taux vert actuel: %.2f, Services actifs: %d",
                    carbonRate, greenRate, activeServices
            );
        });
    }

    /**
     * NOUVEL ENDPOINT - Résumé complet des métriques
     */
    @GetMapping("/summary")
    public Mono<String> getMetricsSummary() {
        return monitoringService.getMetricsSummary();
    }

    /**
     * ENDPOINTS EXISTANTS - fonctionnent toujours pareil
     */
    @GetMapping("/metrics/{serviceName}")
    public Flux<ServiceMetric> getServiceMetrics(
            @PathVariable String serviceName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return monitoringService.getServiceMetrics(serviceName, since);
    }

    @GetMapping("/metrics/{serviceName}/latest")
    public Mono<ServiceMetric> getLatestMetric(@PathVariable String serviceName) {
        return monitoringService.getLatestMetric(serviceName);
    }

    /**
     * NOUVEL ENDPOINT - Gestion des services actifs
     */
    @PostMapping("/service/register")
    public Mono<String> registerService() {
        return Mono.fromRunnable(() -> metricsService.registerActiveService())
                .then(Mono.just("Service enregistré"));
    }

    @PostMapping("/service/unregister")
    public Mono<String> unregisterService() {
        return Mono.fromRunnable(() -> metricsService.unregisterActiveService())
                .then(Mono.just("Service désenregistré"));
    }
}