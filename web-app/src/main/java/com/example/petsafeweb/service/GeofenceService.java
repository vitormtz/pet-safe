package com.example.petsafeweb.service;

import com.example.petsafeweb.dto.ErrorResponse;
import com.example.petsafeweb.dto.GeofenceDataResponse;
import com.example.petsafeweb.dto.GeofenceRequest;
import com.example.petsafeweb.dto.GeofenceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service para gerenciamento de geofences via API
 */
@Slf4j
@Service
public class GeofenceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    public GeofenceService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Busca o geofence do usuário autenticado
     */
    public GeofenceResponse getGeofence(String accessToken) {
        try {
            String url = apiBaseUrl + "/api/v1/geofence";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<GeofenceDataResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                GeofenceDataResponse.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }

            return null;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (HttpClientErrorException e) {
            log.error("Erro ao buscar geofence: {}", e.getMessage());
            throw new RuntimeException("Erro ao buscar geofence: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar geofence", e);
            throw new RuntimeException("Erro ao buscar geofence");
        }
    }

    /**
     * Cria um novo geofence
     */
    public GeofenceResponse createGeofence(GeofenceRequest request, String accessToken) {
        try {
            String url = apiBaseUrl + "/api/v1/geofence";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeofenceRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<GeofenceDataResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                GeofenceDataResponse.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }

            throw new RuntimeException("Resposta vazia ao criar geofence");
        } catch (HttpClientErrorException e) {
            log.error("Erro ao criar geofence: {}", e.getResponseBodyAsString());
            try {
                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                throw new RuntimeException(error.getError());
            } catch (Exception ex) {
                throw new RuntimeException("Erro ao criar geofence: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao criar geofence", e);
            throw new RuntimeException("Erro ao criar geofence");
        }
    }

    /**
     * Atualiza o geofence existente
     */
    public GeofenceResponse updateGeofence(GeofenceRequest request, String accessToken) {
        try {
            String url = apiBaseUrl + "/api/v1/geofence";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeofenceRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<GeofenceDataResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                requestEntity,
                GeofenceDataResponse.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }

            throw new RuntimeException("Resposta vazia ao atualizar geofence");
        } catch (HttpClientErrorException e) {
            log.error("Erro ao atualizar geofence: {}", e.getResponseBodyAsString());
            try {
                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                throw new RuntimeException(error.getError());
            } catch (Exception ex) {
                throw new RuntimeException("Erro ao atualizar geofence: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar geofence", e);
            throw new RuntimeException("Erro ao atualizar geofence");
        }
    }

    /**
     * Deleta o geofence do usuário
     */
    public void deleteGeofence(String accessToken) {
        try {
            String url = apiBaseUrl + "/api/v1/geofence";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                requestEntity,
                Void.class
            );

        } catch (HttpClientErrorException e) {
            log.error("Erro ao deletar geofence: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao deletar geofence: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar geofence", e);
            throw new RuntimeException("Erro ao deletar geofence");
        }
    }
}
