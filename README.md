# 🌱 LowCarb Monitoring

**Part of the [LowCarb](https://github.com/Eddvance/LowCarb) application**

Metrics and observability service for the LowCarb microservices ecosystem.

## 🎯 Role

Exposes application metrics collected via Micrometer, scraped by Prometheus and visualized in Grafana dashboards.

**Metrics include:**
- HTTP request rates & response times
- JVM metrics (memory, GC, threads)
- Service health status
- Custom business metrics

## 🛠️ Tech Stack

- Java 17
- Spring Boot 3.x
- Micrometer
- Prometheus
- Grafana
- Eureka Client

## 🚀 Running

This service is part of the LowCarb microservices ecosystem. See the main [LowCarb repository](https://github.com/Eddvance/LowCarb) for full setup instructions with Docker Compose.

**Access:**
| Service | URL |
|---------|-----|
| Metrics endpoint | http://localhost:8082/actuator/prometheus |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |

> Grafana credentials: `admin` / `admin`

## 📝 License

MIT License - see [LICENSE](LICENSE) for details.
