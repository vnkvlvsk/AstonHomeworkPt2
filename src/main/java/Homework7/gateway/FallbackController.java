package Homework7.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/users")
    public ResponseEntity<Map<String, String>> users() {
        return degraded("user-service");
    }

    @RequestMapping("/notifications")
    public ResponseEntity<Map<String, String>> notifications() {
        return degraded("notification-service");
    }

    private ResponseEntity<Map<String, String>> degraded(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "service", service,
                        "message", "Сервис временно недоступен, попробуйте позже."
                ));
    }
}
