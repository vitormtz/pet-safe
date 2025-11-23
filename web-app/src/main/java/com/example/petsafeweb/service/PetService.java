package com.example.petsafeweb.service;

import com.example.petsafeweb.dto.ErrorResponse;
import com.example.petsafeweb.dto.PetRequest;
import com.example.petsafeweb.dto.PetResponse;
import com.example.petsafeweb.dto.PetsListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service para gerenciamento de pets via API
 */
@Slf4j
@Service
public class PetService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    @Value("${petsafe.api.endpoints.pets}")
    private String petsEndpoint;

    public PetService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Lista todos os pets do usuário autenticado
     *
     * @param accessToken Token de acesso do usuário
     * @return Lista de pets
     * @throws Exception Se houver erro na comunicação com a API
     */
    public List<PetResponse> listPets(String accessToken) throws Exception {
        String url = apiBaseUrl + petsEndpoint;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<PetsListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    PetsListResponse.class);

            PetsListResponse petsListResponse = response.getBody();

            if (petsListResponse == null || petsListResponse.getData() == null) {
                return List.of();
            }

            return petsListResponse.getData();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao listar pets. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new Exception("Sessão expirada. Faça login novamente.");
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Não é erro, apenas não tem pets ainda
                return List.of();
            }
            throw new Exception("Erro ao listar pets. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Erro inesperado ao listar pets", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * Busca detalhes de um pet específico
     *
     * @param petId       ID do pet
     * @param accessToken Token de acesso do usuário
     * @return Dados do pet
     * @throws Exception Se houver erro na comunicação com a API
     */
    public PetResponse getPet(Long petId, String accessToken) throws Exception {
        String url = apiBaseUrl + petsEndpoint + "/" + petId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<PetResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    PetResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao buscar pet. Status: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Pet não encontrado.");
            }
            throw new Exception("Erro ao buscar pet. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar pet", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * Cria um novo pet
     *
     * @param petRequest  Dados do pet a ser criado
     * @param accessToken Token de acesso do usuário
     * @return Pet criado
     * @throws Exception Se houver erro na comunicação com a API
     */
    public PetResponse createPet(PetRequest petRequest, String accessToken) throws Exception {
        String url = apiBaseUrl + petsEndpoint;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<PetRequest> request = new HttpEntity<>(petRequest, headers);

            ResponseEntity<PetResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PetResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao criar pet. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            try {
                ErrorResponse errorResponse = objectMapper.readValue(
                        e.getResponseBodyAsString(),
                        ErrorResponse.class);
                throw new Exception(errorResponse.getMessage() != null
                        ? errorResponse.getMessage()
                        : "Erro ao criar pet");
            } catch (Exception parseException) {
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new Exception("Dados inválidos. Verifique as informações e tente novamente.");
                } else {
                    throw new Exception("Erro ao criar pet. Tente novamente mais tarde.");
                }
            }

        } catch (Exception e) {
            log.error("Erro inesperado ao criar pet", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * Atualiza dados de um pet
     *
     * @param petId       ID do pet a ser atualizado
     * @param petRequest  Novos dados do pet
     * @param accessToken Token de acesso do usuário
     * @return Pet atualizado
     * @throws Exception Se houver erro na comunicação com a API
     */
    public PetResponse updatePet(Long petId, PetRequest petRequest, String accessToken) throws Exception {
        String url = apiBaseUrl + petsEndpoint + "/" + petId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<PetRequest> request = new HttpEntity<>(petRequest, headers);

            ResponseEntity<PetResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    request,
                    PetResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao atualizar pet. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            try {
                ErrorResponse errorResponse = objectMapper.readValue(
                        e.getResponseBodyAsString(),
                        ErrorResponse.class);
                throw new Exception(errorResponse.getMessage() != null
                        ? errorResponse.getMessage()
                        : "Erro ao atualizar pet");
            } catch (Exception parseException) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new Exception("Pet não encontrado.");
                } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    String errorBody = e.getResponseBodyAsString();
                    throw new Exception("Dados inválidos. Detalhes: " + errorBody);
                } else {
                    throw new Exception("Erro ao atualizar pet. Tente novamente mais tarde.");
                }
            }

        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar pet", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * Exclui um pet
     *
     * @param petId       ID do pet a ser excluído
     * @param accessToken Token de acesso do usuário
     * @throws Exception Se houver erro na comunicação com a API
     */
    public void deletePet(Long petId, String accessToken) throws Exception {
        String url = apiBaseUrl + petsEndpoint + "/" + petId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<?> request = new HttpEntity<>(headers);

            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Void.class);

        } catch (HttpClientErrorException e) {
            log.error("Erro ao excluir pet. Status: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Pet não encontrado.");
            }
            throw new Exception("Erro ao excluir pet. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Erro inesperado ao excluir pet", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }
}
