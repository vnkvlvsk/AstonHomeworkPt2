package ConfigServer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=config-server,native"
)
class ConfigServerApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void servesUserServiceConfigFromConfigRepo() {
        ResponseEntity<String> response = restTemplate.getForEntity("/user-service/default", String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().contains("server.forward-headers-strategy"));
    }
}
