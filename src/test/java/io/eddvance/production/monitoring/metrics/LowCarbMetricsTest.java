package io.eddvance.production.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de la classe LowCarbMetrics")
class LowCarbMetricsTest {

    @Mock
    private MeterRegistry registry;

    @Mock
    private Counter carbonRateCounter;

    @Mock
    private Counter greenRateCounter;

    @Mock
    private Counter errorCounter;

    @Mock
    private Timer carbonRateTimer;

    @Mock
    private Timer greenRateTimer;

    private LowCarbMetrics metrics;

    @BeforeEach
    void setUp() {
        // CORRECTION: Mock avec Tags.of() au lieu de anyString()
        when(registry.counter(eq("lowcarb_requests_total"),
                eq(Tags.of("rate_type", "carbon", "service", "lowcarb"))))
                .thenReturn(carbonRateCounter);

        when(registry.counter(eq("lowcarb_requests_total"),
                eq(Tags.of("rate_type", "green", "service", "lowcarb"))))
                .thenReturn(greenRateCounter);

        when(registry.counter(eq("lowcarb_errors_total"),
                eq(Tags.of("service", "lowcarb"))))
                .thenReturn(errorCounter);

        when(registry.timer(eq("lowcarb_response_duration"),
                eq(Tags.of("rate_type", "carbon", "service", "lowcarb"))))
                .thenReturn(carbonRateTimer);

        when(registry.timer(eq("lowcarb_response_duration"),
                eq(Tags.of("rate_type", "green", "service", "lowcarb"))))
                .thenReturn(greenRateTimer);

        // CORRECTION: Mock des gauges
        when(registry.gauge(eq("lowcarb_rate_current"),
                eq(Tags.of("rate_type", "carbon", "service", "lowcarb")),
                any(), any())).thenReturn(null);

        when(registry.gauge(eq("lowcarb_rate_current"),
                eq(Tags.of("rate_type", "green", "service", "lowcarb")),
                any(), any())).thenReturn(null);

        when(registry.gauge(eq("lowcarb_services_active"),
                eq(Tags.of("service", "lowcarb")),
                any(), any())).thenReturn(null);

        metrics = new LowCarbMetrics(registry);
    }

    @Nested
    @DisplayName("Tests des compteurs de requêtes")
    class RequestCountersTests {

        @Test
        @DisplayName("Doit incrémenter le compteur de taux carbone")
        void shouldIncrementCarbonRateCounter() {
            metrics.incrementCarbonRateRequests();
            verify(carbonRateCounter).increment();
        }

        @Test
        @DisplayName("Doit incrémenter le compteur de taux vert")
        void shouldIncrementGreenRateCounter() {
            metrics.incrementGreenRateRequests();
            verify(greenRateCounter).increment();
        }

        @Test
        @DisplayName("Doit incrémenter le compteur d'erreurs")
        void shouldIncrementErrorCounter() {
            metrics.incrementErrors("TEST_ERROR");
            verify(errorCounter).increment();
        }
    }

    @Nested
    @DisplayName("Tests des valeurs de taux")
    class RateValuesTests {

        @Test
        @DisplayName("Doit mettre à jour et récupérer le dernier taux carbone")
        void shouldUpdateAndGetLastCarbonRate() {
            double testRate = 42.0;
            metrics.setLastCarbonRate(testRate);
            assertThat(metrics.getLastCarbonRate()).isEqualTo(testRate);
        }

        @Test
        @DisplayName("Doit mettre à jour et récupérer le dernier taux vert")
        void shouldUpdateAndGetLastGreenRate() {
            double testRate = 84.0;
            metrics.setLastGreenRate(testRate);
            assertThat(metrics.getLastGreenRate()).isEqualTo(testRate);
        }

        @Test
        @DisplayName("Ne doit pas accepter un taux carbone négatif")
        void shouldNotAcceptNegativeCarbonRate() {
            double initialRate = metrics.getLastCarbonRate();
            metrics.setLastCarbonRate(-42.0);
            assertThat(metrics.getLastCarbonRate()).isEqualTo(initialRate);
        }
    }

    @Nested
    @DisplayName("Tests du compteur de services actifs")
    class ActiveServicesTests {

        @Test
        @DisplayName("Doit gérer correctement l'incrémentation des services actifs")
        void shouldIncrementActiveServices() {
            metrics.setActiveServicesCount(1);
            metrics.incrementActiveServices();
            assertThat(metrics.getActiveServicesCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Doit gérer correctement la décrémentation des services actifs")
        void shouldDecrementActiveServices() {
            metrics.setActiveServicesCount(2);
            metrics.decrementActiveServices();
            assertThat(metrics.getActiveServicesCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Ne doit pas permettre un compte négatif de services actifs")
        void shouldNotAllowNegativeActiveServices() {
            metrics.setActiveServicesCount(0);
            metrics.decrementActiveServices();
            assertThat(metrics.getActiveServicesCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Tests des timers de réponse")
    class ResponseTimerTests {

        @Test
        @DisplayName("Doit enregistrer le temps de réponse pour le taux carbone")
        void shouldRecordCarbonRateResponseTime() {
            Duration testDuration = Duration.ofMillis(100);
            metrics.recordCarbonRateResponseTime(testDuration);
            verify(carbonRateTimer).record(testDuration);
        }

        @Test
        @DisplayName("Doit enregistrer le temps de réponse pour le taux vert")
        void shouldRecordGreenRateResponseTime() {
            Duration testDuration = Duration.ofMillis(200);
            metrics.recordGreenRateResponseTime(testDuration);
            verify(greenRateTimer).record(testDuration);
        }
    }

    @Nested
    @DisplayName("Tests des méthodes utilitaires d'enregistrement")
    class RecordUtilityMethodsTests {

        @Test
        @DisplayName("Doit enregistrer une requête carbone complète")
        void shouldRecordCompleteCarbonRateRequest() {
            double rate = 42.0;
            Duration duration = Duration.ofMillis(100);

            metrics.recordCarbonRateRequest(rate, duration);

            verify(carbonRateCounter).increment();
            assertThat(metrics.getLastCarbonRate()).isEqualTo(rate);
            verify(carbonRateTimer).record(duration);
        }

        @Test
        @DisplayName("Doit enregistrer une requête verte complète")
        void shouldRecordCompleteGreenRateRequest() {
            double rate = 84.0;
            Duration duration = Duration.ofMillis(200);

            metrics.recordGreenRateRequest(rate, duration);

            verify(greenRateCounter).increment();
            assertThat(metrics.getLastGreenRate()).isEqualTo(rate);
            verify(greenRateTimer).record(duration);
        }
    }
}