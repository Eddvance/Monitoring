package io.eddvance.production.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("service_metrics")
public class ServiceMetric {
    @Id
    private Long id;
    private String serviceName;
    private String metricName;
    private Double metricValue;
    private String status;
    private LocalDateTime timestamp;

    public ServiceMetric() {
    }

    public ServiceMetric(String serviceName, String metricName, Double metricValue, String status) {
        this.serviceName = serviceName;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
