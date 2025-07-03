package io.eddvance.production.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LowCarbMetrics {

    private final Counter carbonRateRequestCounter;
    private final Counter greenRateRequestCounter;
    private final Counter errorCounter;
    private final AtomicReference<Double> lastCarbonRateValue;
    private final AtomicReference<Double> lastGreenRateValue;
    private final AtomicReference<Long> activeServicesCount;
    private final Timer carbonRateResponseTimer;
    private final Timer greenRateResponseTimer;

    public LowCarbMetrics(MeterRegistry registry) {
        // Initialisation des valeurs atomiques
        this.lastCarbonRateValue = new AtomicReference<>(0.0);
        this.lastGreenRateValue = new AtomicReference<>(0.0);
        this.activeServicesCount = new AtomicReference<>(0L);

        // Compteurs avec Tags.of() pour créer les tags
        this.carbonRateRequestCounter = registry.counter("lowcarb_requests_total",
                Tags.of("rate_type", "carbon", "service", "lowcarb"));

        this.greenRateRequestCounter = registry.counter("lowcarb_requests_total",
                Tags.of("rate_type", "green", "service", "lowcarb"));

        this.errorCounter = registry.counter("lowcarb_errors_total",
                Tags.of("service", "lowcarb"));

        // Gauges avec Tags.of() - CORRECTION PRINCIPALE
        registry.gauge("lowcarb_rate_current",
                Tags.of("rate_type", "carbon", "service", "lowcarb"),
                this.lastCarbonRateValue,
                ref -> ref.get());

        registry.gauge("lowcarb_rate_current",
                Tags.of("rate_type", "green", "service", "lowcarb"),
                this.lastGreenRateValue,
                ref -> ref.get());

        registry.gauge("lowcarb_services_active",
                Tags.of("service", "lowcarb"),
                this.activeServicesCount,
                ref -> ref.get().doubleValue());

        // Timers avec Tags.of()
        this.carbonRateResponseTimer = registry.timer("lowcarb_response_duration",
                Tags.of("rate_type", "carbon", "service", "lowcarb"));

        this.greenRateResponseTimer = registry.timer("lowcarb_response_duration",
                Tags.of("rate_type", "green", "service", "lowcarb"));
    }

    /**
     * Incrémente le compteur de requêtes carbone
     */
    public void incrementCarbonRateRequests() {
        this.carbonRateRequestCounter.increment();
    }

    /**
     * Incrémente le compteur de requêtes vertes
     */
    public void incrementGreenRateRequests() {
        this.greenRateRequestCounter.increment();
    }

    /**
     * Incrémente le compteur d'erreurs
     */
    public void incrementErrors(String errorType) {
        this.errorCounter.increment();
    }

    /**
     * Met à jour le dernier taux carbone
     */
    public void setLastCarbonRate(Double rate) {
        if (rate != null && rate >= 0) {
            this.lastCarbonRateValue.set(rate);
        }
    }

    /**
     * Met à jour le dernier taux vert
     */
    public void setLastGreenRate(Double rate) {
        if (rate != null && rate >= 0) {
            this.lastGreenRateValue.set(rate);
        }
    }

    /**
     * Met à jour le nombre de services actifs
     */
    public void setActiveServicesCount(long count) {
        this.activeServicesCount.set(count);
    }

    /**
     * Incrémente le nombre de services actifs
     */
    public void incrementActiveServices() {
        this.activeServicesCount.updateAndGet(current -> current + 1);
    }

    /**
     * Décrémente le nombre de services actifs
     */
    public void decrementActiveServices() {
        this.activeServicesCount.updateAndGet(current -> Math.max(0L, current - 1));
    }

    /**
     * Enregistre le temps de réponse pour une requête carbone
     */
    public void recordCarbonRateResponseTime(Duration duration) {
        this.carbonRateResponseTimer.record(duration);
    }

    /**
     * Enregistre le temps de réponse pour une requête verte
     */
    public void recordGreenRateResponseTime(Duration duration) {
        this.greenRateResponseTimer.record(duration);
    }

    /**
     * Méthode utilitaire pour enregistrer une requête complète avec toutes les métriques
     */
    public void recordCarbonRateRequest(Double rate, Duration responseTime) {
        incrementCarbonRateRequests();
        setLastCarbonRate(rate);
        recordCarbonRateResponseTime(responseTime);
    }

    /**
     * Méthode utilitaire pour enregistrer une requête complète avec toutes les métriques
     */
    public void recordGreenRateRequest(Double rate, Duration responseTime) {
        incrementGreenRateRequests();
        setLastGreenRate(rate);
        recordGreenRateResponseTime(responseTime);
    }

    /**
     * Obtient le dernier taux carbone
     */
    public double getLastCarbonRate() {
        return this.lastCarbonRateValue.get();
    }

    /**
     * Obtient le dernier taux vert
     */
    public double getLastGreenRate() {
        return this.lastGreenRateValue.get();
    }

    /**
     * Obtient le nombre de services actifs
     */
    public long getActiveServicesCount() {
        return this.activeServicesCount.get();
    }
}