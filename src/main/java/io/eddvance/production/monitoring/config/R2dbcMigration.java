package io.eddvance.production.monitoring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class R2dbcMigration {

    private static final Logger log = LoggerFactory.getLogger(R2dbcMigration.class);

    private final DatabaseClient databaseClient;

    public R2dbcMigration(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createTables() {
        log.info("🚀 Démarrage de la migration de base de données...");
        databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS service_metrics (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            service_name VARCHAR(255) NOT NULL,
                            metric_name VARCHAR(255) NOT NULL,
                            metric_value DOUBLE NOT NULL,
                            status VARCHAR(50),
                            timestamp TIMESTAMP NOT NULL
                        )
                        """)
                .then()
                .doOnSuccess(v -> log.info("✅ Table service_metrics créée avec succès"))
                .doOnError(e -> log.error("❌ Erreur lors de la création de la table", e))
                .block();

        log.info("Migration terminée");
    }
}