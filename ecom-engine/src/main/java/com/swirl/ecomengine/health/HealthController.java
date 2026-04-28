package com.swirl.ecomengine.health;

import com.swirl.ecomengine.product.service.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final ProductService productService;

    /*@PersistenceContext
    private EntityManager entityManager;*/

    private final Instant startTime = Instant.now();

    public HealthController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health/details")
    public ResponseEntity<Map<String, Object>> healthDetails() {

        Map<String, Object> details = new HashMap<>();

        // Basic status
        details.put("status", "UP");

        // Uptime
        Duration uptime = Duration.between(startTime, Instant.now());
        details.put("uptime", formatUptime(uptime));

        // Database check TODO implement backend
        /*boolean dbUp = isDatabaseUp();
        details.put("database", dbUp ? "UP" : "DOWN");*/

        // Last product update
        LocalDateTime lastUpdated = productService.getLastUpdated();
        details.put("lastUpdated", lastUpdated);

        // Version
        details.put("version", "1.0.0");

        return ResponseEntity.ok(details);
    }

    /*private boolean isDatabaseUp() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }*/

    private String formatUptime(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        return hours + "h " + minutes + "m " + seconds + "s";
    }
}
