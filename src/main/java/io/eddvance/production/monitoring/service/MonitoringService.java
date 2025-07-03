package io.eddvance.production.monitoring.service;

import io.eddvance.production.monitoring.metrics.LowCarbMetrics;
import io.eddvance.production.monitoring.model.ServiceMetric;
import io.eddvance.production.monitoring.repository.ServiceMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);
    private final ServiceMetricRepository metricRepository;
    private final LowCarbMetrics lowCarbMetrics;

    public MonitoringService(ServiceMetricRepository metricRepository, LowCarbMetrics lowCarbMetrics) {
        this.metricRepository = metricRepository;
        this.lowCarbMetrics = lowCarbMetrics;
    }


    public Mono<ServiceMetric> saveMetric(String serviceName, String metricName, Double value, String status) {
        Instant start = Instant.now();

        ServiceMetric metric = new ServiceMetric(serviceName, metricName, value, status);

        return metricRepository.save(metric)
                .doOnSuccess(saved -> {
                    Duration saveTime = Duration.between(start, Instant.now());

                    log.info("Métrique enregistrée : {} - {} = {} ({})", serviceName, metricName, value, status);


                    if ("lowcarb".equals(serviceName)) {
                        trackLowCarbMetric(metricName, value, saveTime);
                    }
                })
                .doOnError(error -> {
                    log.error("Erreur lors de l'enregistrement de la métrique", error);
                    lowCarbMetrics.incrementErrors("database_save_error");
                });
    }


    private void trackLowCarbMetric(String metricName, Double value, Duration responseTime) {
        switch (metricName.toLowerCase()) {
            case "carbon_rate":
            case "taux_carbone":
                lowCarbMetrics.recordCarbonRateRequest(value, responseTime);
                break;
            case "green_rate":
            case "taux_vert":
                lowCarbMetrics.recordGreenRateRequest(value, responseTime);
                break;
            case "active_services":
                lowCarbMetrics.setActiveServicesCount(value.longValue());
                break;
            default:
                log.debug("Métrique non spécifique trackée: {}", metricName);
        }
    }


    public Flux<ServiceMetric> getServiceMetrics(String serviceName, LocalDateTime since) {
        return metricRepository.findByServiceNameAndTimestampAfter(serviceName, since)
                .doOnError(error -> {
                    log.error("Erreur lors de la récupération des métriques pour {}", serviceName, error);
                    lowCarbMetrics.incrementErrors("database_read_error");
                });
    }


    public Mono<ServiceMetric> getLatestMetric(String serviceName) {
        return metricRepository.findLatestByServiceName(serviceName)
                .doOnError(error -> {
                    log.error("Erreur lors de la récupération de la dernière métrique pour {}", serviceName, error);
                    lowCarbMetrics.incrementErrors("database_read_error");
                });
    }


    public Mono<String> getMetricsSummary() {
        return Mono.fromCallable(() -> {
            StringBuilder summary = new StringBuilder();
            summary.append("=== Résumé des métriques LowCarb ===\n");
            summary.append("Dernier taux carbone: ").append(lowCarbMetrics.getLastCarbonRate()).append("\n");
            summary.append("Dernier taux vert: ").append(lowCarbMetrics.getLastGreenRate()).append("\n");
            summary.append("Services actifs: ").append(lowCarbMetrics.getActiveServicesCount()).append("\n");
            return summary.toString();
        });
    }
}