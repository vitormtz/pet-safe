package com.example.petsafeweb.controller;

import com.example.petsafeweb.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final RestTemplate restTemplate;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    public AlertController(AlertService alertService, RestTemplate restTemplate) {
        this.alertService = alertService;
        this.restTemplate = restTemplate;
    }

    /**
     * Retorna a contagem de alertas não lidos
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getUnreadAlertsCount(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            Map<String, Integer> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        Integer count = alertService.getUnreadAlertsCount(accessToken);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Proxy para buscar todos os alertas
     */
    @GetMapping
    public ResponseEntity<?> listAlerts(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return ResponseEntity.ok(Map.of("data", new Object[]{}));
        }

        try {
            String url = apiBaseUrl + "/api/v1/alerts";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao buscar alertas", e);
            return ResponseEntity.ok(Map.of("data", new Object[]{}));
        }
    }

    /**
     * Proxy para marcar alerta como lido
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            String url = apiBaseUrl + "/api/v1/alerts/" + id + "/read";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                requestEntity,
                Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao marcar alerta como lido", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Proxy para marcar todos os alertas como lidos
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            String url = apiBaseUrl + "/api/v1/alerts/read-all";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                requestEntity,
                Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao marcar todos alertas como lidos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
