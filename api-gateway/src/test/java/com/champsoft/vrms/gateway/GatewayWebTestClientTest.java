package com.champsoft.vrms.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

// Start full Spring Boot app on a RANDOM port
// Required because we are testing real HTTP requests to the Gateway
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// Use testing profile
@ActiveProfiles("testing")
class GatewayWebTestClientTest {

    // Inject the random port assigned at runtime
    @LocalServerPort
    private int port;

    // WebTestClient → used for testing reactive web applications (Spring WebFlux)
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {

        // Build HTTP client pointing to running Gateway instance
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Should return 404 for unknown route")
    void shouldReturnNotFoundForUnknownRoute() {

        // ------------------- Act + Assert -------------------
        // Call a route that does NOT exist
        webTestClient.get()
                .uri("/api/unknown")
                .exchange()

                // Expect HTTP 404 (Not Found)
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should expose actuator health endpoint")
    void shouldExposeActuatorHealthEndpoint() {

        // ------------------- Act + Assert -------------------
        // Call Spring Boot actuator endpoint
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()

                // Expect HTTP 200 OK
                .expectStatus().isOk();
    }
}