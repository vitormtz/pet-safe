package com.example.petsafeweb.service;

import com.example.petsafeweb.dto.AlertCountResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AlertService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    public AlertService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Busca a contagem de alertas não lidos
     */
    public Integer getUnreadAlertsCount(String accessToken) {
        try {
            String url = apiBaseUrl + "/api/v1/alerts/count";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<AlertCountResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                AlertCountResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getCount();
            }

            return 0;
        } catch (Exception e) {
            log.error("Erro ao buscar contagem de alertas", e);
            return 0;
        }
    }
}
