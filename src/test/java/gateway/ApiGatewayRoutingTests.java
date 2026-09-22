package gateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

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
                "spring.cloud.gateway.server.webmvc.routes[1].id=notification-service",
                "spring.cloud.gateway.server.webmvc.routes[1].uri=lb://notification-service",
                "spring.cloud.gateway.server.webmvc.routes[1].predicates[0]=Path=/api/notifications/**"
        }
)
class ApiGatewayRoutingTests {

    private static HttpServer userServiceStub;
    private static HttpServer notificationServiceStub;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startFakeBackends() throws IOException {
        userServiceStub = stubServer("user-service response");
        notificationServiceStub = stubServer("notification-service response");
    }

    @AfterAll
    static void stopFakeBackends() {
        userServiceStub.stop(0);
        notificationServiceStub.stop(0);
    }

    @DynamicPropertySource
    static void registerFakeInstances(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.discovery.client.simple.instances.user-service[0].uri",
                () -> "http://localhost:" + userServiceStub.getAddress().getPort());
        registry.add("spring.cloud.discovery.client.simple.instances.notification-service[0].uri",
                () -> "http://localhost:" + notificationServiceStub.getAddress().getPort());
    }

    private static HttpServer stubServer(String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        server.start();
        return server;
    }

    @Test
    void routesToUserServiceInstance() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/1", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user-service response", response.getBody());
    }

    @Test
    void routesToNotificationServiceInstance() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/notifications", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("notification-service response", response.getBody());
    }

    @Test
    void doesNotCrossRouteBetweenServices() {
        ResponseEntity<String> usersResponse = restTemplate.getForEntity("/api/users/1", String.class);
        ResponseEntity<String> notificationsResponse = restTemplate.getForEntity("/api/notifications", String.class);

        assertTrue(usersResponse.getBody().startsWith("user-service"));
        assertTrue(notificationsResponse.getBody().startsWith("notification-service"));
    }
}
