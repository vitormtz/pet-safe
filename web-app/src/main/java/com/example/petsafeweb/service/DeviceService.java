package com.example.petsafeweb.service;

import com.example.petsafeweb.dto.DeviceRequest;
import com.example.petsafeweb.dto.DeviceResponse;
import com.example.petsafeweb.dto.DevicesListResponse;
import com.example.petsafeweb.dto.ErrorResponse;
import com.example.petsafeweb.dto.LocationResponse;
import com.example.petsafeweb.dto.LocationListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service para gerenciamento de dispositivos via API
 */
@Slf4j
@Service
public class DeviceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    @Value("${petsafe.api.endpoints.devices}")
    private String devicesEndpoint; // Deve ser /devices no application.properties

    public DeviceService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    // --- CREATE DEVICE (func CreateDevice) ---
    public DeviceResponse createDevice(DeviceRequest deviceRequest, String accessToken) throws Exception {
        String url = apiBaseUrl + devicesEndpoint;

        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DeviceRequest> request = new HttpEntity<>(deviceRequest, headers);

            ResponseEntity<DeviceResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, DeviceResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao criar dispositivo. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            // Tratamento de erro semelhante ao PetService
            try {
                ErrorResponse errorResponse = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                throw new Exception(errorResponse.getMessage() != null
                        ? errorResponse.getMessage()
                        : "Erro ao criar dispositivo (serial duplicado?)");
            } catch (Exception parseException) {
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new Exception("Dados inválidos ou serial duplicado. Verifique as informações.");
                } else {
                    throw new Exception("Erro ao criar dispositivo. Tente novamente mais tarde.");
                }
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao criar dispositivo", e);
            throw new Exception("Erro ao conectar com o servidor.");
        }
    }

    // --- LIST DEVICES (func ListDevices) ---
    public List<DeviceResponse> listDevices(String accessToken) throws Exception {
        String url = apiBaseUrl + devicesEndpoint;

        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<DevicesListResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, DevicesListResponse.class);

            DevicesListResponse listResponse = response.getBody();
            return listResponse != null && listResponse.getData() != null ? listResponse.getData() : List.of();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao listar dispositivos. Status: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new Exception("Sessão expirada. Faça login novamente.");
            }
            return List.of(); // Retorna vazio se não houver dispositivos
        } catch (Exception e) {
            log.error("Erro inesperado ao listar dispositivos", e);
            throw new Exception("Erro ao conectar com o servidor.");
        }
    }

    // --- DEVICE STATUS (func DeviceStatus) ---
    public DeviceResponse getDeviceStatus(Long deviceId, String accessToken) throws Exception {
        String url = apiBaseUrl + devicesEndpoint + "/" + deviceId;

        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<DeviceResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, DeviceResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao buscar status do dispositivo. Status: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Dispositivo não encontrado.");
            }
            throw new Exception("Erro ao buscar status do dispositivo.");
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar status do dispositivo", e);
            throw new Exception("Erro ao conectar com o servidor.");
        }
    }

    // --- UPDATE DEVICE (func UpdateDevice) ---
    public DeviceResponse updateDevice(Long deviceId, DeviceRequest deviceRequest, String accessToken)
            throws Exception {
        String url = apiBaseUrl + devicesEndpoint + "/" + deviceId;

        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DeviceRequest> request = new HttpEntity<>(deviceRequest, headers);

            // PATCH é o método usado no Go para UpdateDevice
            ResponseEntity<DeviceResponse> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, request, DeviceResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao atualizar dispositivo. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Dispositivo não encontrado.");
            }
            throw new Exception("Erro ao atualizar dispositivo. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar dispositivo", e);
            throw new Exception("Erro ao conectar com o servidor.");
        }
    }

    // --- DELETE DEVICE (func DeleteDevice) ---
    public void deleteDevice(Long deviceId, String accessToken) throws Exception {
        String url = apiBaseUrl + devicesEndpoint + "/" + deviceId;

        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        } catch (HttpClientErrorException e) {
            log.error("Erro ao excluir dispositivo. Status: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Dispositivo não encontrado.");
            }
            throw new Exception("Erro ao excluir dispositivo. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Erro inesperado ao excluir dispositivo", e);
            throw new Exception("Erro ao conectar com o servidor.");
        }
    }

    // --- LIST DEVICE LOCATIONS (func ListDeviceLocations) ---
    /**
     * Lista as localizações mais recentes de um dispositivo.
     * Mapeia para o endpoint Go: GET /devices/{id}/locations/{limit}
     */
    public List<LocationResponse> listDeviceLocations(Long deviceId, int limit, String accessToken) throws Exception {
        // Assume uma rota como /devices/{id}/locations/{limit} (Baseado no devices.go)
        String url = apiBaseUrl + devicesEndpoint + "/" + deviceId + "/locations/" + limit;

        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            // CORREÇÃO: Usando o novo DTO wrapper LocationListResponse
            ResponseEntity<LocationListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    LocationListResponse.class // Usa o DTO wrapper
            );

            LocationListResponse listResponse = response.getBody();
            // Retorna a lista de localizações (campo 'data' do JSON) ou uma lista vazia
            return listResponse != null && listResponse.getData() != null ? listResponse.getData() : List.of();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao listar localizações do dispositivo. Status: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Dispositivo não encontrado.");
            }
            throw new Exception("Erro ao listar localizações. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Erro inesperado ao listar localizações", e);
            throw new Exception("Erro ao conectar com o servidor.");
        }
    }
}