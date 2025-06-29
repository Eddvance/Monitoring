package io.eddvance.production.monitoring.repository;

import io.eddvance.production.monitoring.model.ServiceMetric;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ServiceMetricRepository extends ReactiveCrudRepository<ServiceMetric, Long> {

    Flux<ServiceMetric> findByServiceNameAndMetricName(String serviceName, String metricName);

    Flux<ServiceMetric> findByServiceNameAndTimestampAfter(String serviceName, LocalDateTime since);

    @Query("SELECT * FROM service_metrics WHERE service_name = :serviceName ORDER BY timestamp DESC LIMIT 1")
    Mono<ServiceMetric> findLatestByServiceName(String serviceName);
}