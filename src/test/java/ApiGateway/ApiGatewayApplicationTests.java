package ApiGateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=api-gateway",
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false",
                "spring.cloud.gateway.server.webmvc.routes[0].id=user-service",
                "spring.cloud.gateway.server.webmvc.routes[0].uri=lb://user-service",
                "spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/api/users/**",
                "spring.cloud.gateway.server.webmvc.routes[0].filters[0].name=CircuitBreaker",
                "spring.cloud.gateway.server.webmvc.routes[0].filters[0].args.id=user-service-cb",
                "spring.cloud.gateway.server.webmvc.routes[0].filters[0].args.fallbackUri=forward:/fallback/users",
                "spring.cloud.gateway.server.webmvc.routes[1].id=notification-service",
                "spring.cloud.gateway.server.webmvc.routes[1].uri=lb://notification-service",
                "spring.cloud.gateway.server.webmvc.routes[1].predicates[0]=Path=/api/notifications/**",
                "spring.cloud.gateway.server.webmvc.routes[1].filters[0].name=CircuitBreaker",
                "spring.cloud.gateway.server.webmvc.routes[1].filters[0].args.id=notification-service-cb",
                "spring.cloud.gateway.server.webmvc.routes[1].filters[0].args.fallbackUri=forward:/fallback/notifications"
        }
)
class ApiGatewayApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void unmatchedPathReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/nonexistent", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void unreachableUserServiceIsRoutedToFallbackByCircuitBreaker() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/1", String.class);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().contains("user-service"));
    }

    @Test
    void unreachableNotificationServiceIsRoutedToFallbackByCircuitBreaker() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/notifications", String.class);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().contains("notification-service"));
    }
}
