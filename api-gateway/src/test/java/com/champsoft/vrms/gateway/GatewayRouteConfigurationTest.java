package com.champsoft.vrms.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest → loads full application context
// Needed here because we want to test Gateway configuration (routes)
@SpringBootTest

// Use testing profile (ensures test-safe configuration)
@ActiveProfiles("testing")
class GatewayRouteConfigurationTest {

    // RouteLocator → main Spring Cloud Gateway component
    // Provides access to all configured routes
    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Should load all API Gateway routes")
    void shouldLoadAllGatewayRoutes() {

        // ------------------- Act -------------------
        // Retrieve all routes from Gateway
        List<Route> routes = routeLocator.getRoutes()
                .collectList()
                .block(); // convert reactive stream to List

        // ------------------- Assert -------------------
        // Ensure routes are loaded
        assertThat(routes).isNotNull();

        // Expect 4 services in gateway
        assertThat(routes).hasSize(4);
    }

    @Test
    @DisplayName("Should contain route for cars-service")
    void shouldContainCarsServiceRoute() {

        // ------------------- Act -------------------
        Map<String, Route> routes = getRoutesById();

        // ------------------- Assert -------------------
        // Verify route exists
        assertThat(routes).containsKey("cars-service");

        // Verify route target (where traffic is forwarded)
        assertThat(routes.get("cars-service").getUri().toString())
                .isEqualTo("http://localhost:8081");
    }

    @Test
    @DisplayName("Should contain route for owners-service")
    void shouldContainOwnersServiceRoute() {

        // ------------------- Act -------------------
        Map<String, Route> routes = getRoutesById();

        // ------------------- Assert -------------------
        assertThat(routes).containsKey("owners-service");
        assertThat(routes.get("owners-service").getUri().toString())
                .isEqualTo("http://localhost:8082");
    }

    @Test
    @DisplayName("Should contain route for agents-service")
    void shouldContainAgentsServiceRoute() {

        // ------------------- Act -------------------
        Map<String, Route> routes = getRoutesById();

        // ------------------- Assert -------------------
        assertThat(routes).containsKey("agents-service");
        assertThat(routes.get("agents-service").getUri().toString())
                .isEqualTo("http://localhost:8083");
    }

    @Test
    @DisplayName("Should contain route for registration-service")
    void shouldContainRegistrationServiceRoute() {

        // ------------------- Act -------------------
        Map<String, Route> routes = getRoutesById();

        // ------------------- Assert -------------------
        assertThat(routes).containsKey("registration-service");
        assertThat(routes.get("registration-service").getUri().toString())
                .isEqualTo("http://localhost:8084");
    }

    // Helper method → converts list of routes into a Map for easier lookup
    private Map<String, Route> getRoutesById() {

        // Fetch all routes from Gateway
        List<Route> routes = routeLocator.getRoutes()
                .collectList()
                .block();

        // Safety check
        assertThat(routes).isNotNull();

        // Convert List → Map (key = routeId, value = Route)
        return routes.stream()
                .collect(Collectors.toMap(Route::getId, route -> route));
    }
}