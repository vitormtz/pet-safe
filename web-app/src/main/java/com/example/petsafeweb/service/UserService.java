package com.example.petsafeweb.service;

import com.example.petsafeweb.dto.ApiDataResponse;
import com.example.petsafeweb.dto.ErrorResponse;
import com.example.petsafeweb.dto.UpdateProfileRequest;
import com.example.petsafeweb.dto.UserProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service para operações relacionadas ao usuário
 */
@Slf4j
@Service
public class UserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Busca o perfil do usuário atual usando o endpoint /me
     *
     * @param userId ID do usuário (não utilizado, mantido por compatibilidade)
     * @param accessToken Token de acesso
     * @return UserProfileResponse com dados do perfil
     * @throws Exception Se houver erro na comunicação com a API
     */
    public UserProfileResponse getUserProfile(Long userId, String accessToken) throws Exception {
        String url = apiBaseUrl + "/api/v1/me";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Usar ParameterizedTypeReference para deserializar o wrapper ApiDataResponse
            ResponseEntity<ApiDataResponse<UserProfileResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ApiDataResponse<UserProfileResponse>>() {}
            );

            ApiDataResponse<UserProfileResponse> apiResponse = response.getBody();
            UserProfileResponse profile = apiResponse != null ? apiResponse.getData() : null;

            return profile;

        } catch (HttpClientErrorException e) {
            log.error("Erro ao buscar perfil do usuário. Status: {}, Body: {}",
                e.getStatusCode(), e.getResponseBodyAsString());

            String errorMessage = null;
            try {
                ErrorResponse errorResponse = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorResponse.class
                );
                errorMessage = errorResponse.getMessage();
            } catch (Exception parseException) {
                log.debug("Não foi possível parsear a resposta de erro");
            }

            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                throw new Exception(errorMessage);
            }

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new Exception("Sessão expirada. Faça login novamente.");
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception("Usuário não encontrado.");
            } else {
                throw new Exception("Erro ao carregar perfil. Tente novamente mais tarde.");
            }

        } catch (Exception e) {
            if (e.getMessage() != null &&
                (e.getMessage().contains("Sessão expirada") ||
                 e.getMessage().contains("Usuário não encontrado") ||
                 e.getMessage().contains("Erro ao carregar perfil"))) {
                throw e;
            }

            log.error("Erro inesperado ao buscar perfil do usuário", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * Atualiza o perfil do usuário
     *
     * @param userId ID do usuário
     * @param updateRequest Dados a serem atualizados
     * @param accessToken Token de acesso
     * @return UserProfileResponse com dados atualizados
     * @throws Exception Se houver erro na comunicação com a API
     */
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest updateRequest, String accessToken) throws Exception {
        String url = apiBaseUrl + "/api/v1/users/" + userId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<UpdateProfileRequest> request = new HttpEntity<>(updateRequest, headers);

            ResponseEntity<UserProfileResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                UserProfileResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao atualizar perfil do usuário. Status: {}, Body: {}",
                e.getStatusCode(), e.getResponseBodyAsString());

            String errorMessage = null;
            try {
                ErrorResponse errorResponse = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorResponse.class
                );
                errorMessage = errorResponse.getMessage();
            } catch (Exception parseException) {
                log.debug("Não foi possível parsear a resposta de erro");
            }

            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                throw new Exception(errorMessage);
            }

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new Exception("Sessão expirada. Faça login novamente.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new Exception("Dados inválidos. Verifique as informações e tente novamente.");
            } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new Exception("Senha atual incorreta.");
            } else {
                throw new Exception("Erro ao atualizar perfil. Tente novamente mais tarde.");
            }

        } catch (Exception e) {
            if (e.getMessage() != null &&
                (e.getMessage().contains("Sessão expirada") ||
                 e.getMessage().contains("Dados inválidos") ||
                 e.getMessage().contains("Senha atual incorreta") ||
                 e.getMessage().contains("Erro ao atualizar perfil"))) {
                throw e;
            }

            log.error("Erro inesperado ao atualizar perfil do usuário", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }
}
