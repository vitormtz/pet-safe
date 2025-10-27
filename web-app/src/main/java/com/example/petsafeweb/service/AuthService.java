package com.example.petsafeweb.service;

import com.example.petsafeweb.dto.ErrorResponse;
import com.example.petsafeweb.dto.LoginRequest;
import com.example.petsafeweb.dto.LoginResponse;
import com.example.petsafeweb.dto.RegisterRequest;
import com.example.petsafeweb.dto.RegisterResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service para autenticação e registro de usuários via API
 */
@Slf4j
@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${petsafe.api.base-url}")
    private String apiBaseUrl;

    @Value("${petsafe.api.endpoints.register}")
    private String registerEndpoint;

    @Value("${petsafe.api.endpoints.login}")
    private String loginEndpoint;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Registra um novo usuário na API
     *
     * @param registerRequest Dados do usuário a ser cadastrado
     * @return RegisterResponse com os dados do usuário criado
     * @throws Exception Se houver erro na comunicação com a API
     */
    public RegisterResponse registerUser(RegisterRequest registerRequest) throws Exception {
        String url = apiBaseUrl + registerEndpoint;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);

            ResponseEntity<RegisterResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                RegisterResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao registrar usuário. Status: {}, Body: {}",
                e.getStatusCode(), e.getResponseBodyAsString());

            // Tentar parsear a resposta de erro
            try {
                ErrorResponse errorResponse = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorResponse.class
                );
                throw new Exception(errorResponse.getMessage() != null
                    ? errorResponse.getMessage()
                    : "Erro ao registrar usuário");
            } catch (Exception parseException) {
                // Se não conseguir parsear, retornar mensagem genérica
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new Exception("Dados inválidos. Verifique as informações e tente novamente.");
                } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                    throw new Exception("E-mail já cadastrado. Tente fazer login ou use outro e-mail.");
                } else {
                    throw new Exception("Erro ao registrar usuário. Tente novamente mais tarde.");
                }
            }

        } catch (Exception e) {
            log.error("Erro inesperado ao registrar usuário", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * Realiza login do usuário na API
     *
     * @param loginRequest Credenciais do usuário
     * @return LoginResponse com os dados do usuário e tokens
     * @throws Exception Se houver erro na autenticação
     */
    public LoginResponse loginUser(LoginRequest loginRequest) throws Exception {
        String url = apiBaseUrl + loginEndpoint;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<LoginResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                LoginResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erro ao fazer login. Status: {}, Body: {}",
                e.getStatusCode(), e.getResponseBodyAsString());

            // Tentar parsear a resposta de erro
            try {
                ErrorResponse errorResponse = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorResponse.class
                );
                throw new Exception(errorResponse.getMessage() != null
                    ? errorResponse.getMessage()
                    : "Erro ao fazer login");
            } catch (Exception parseException) {
                // Se não conseguir parsear, retornar mensagem genérica
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new Exception("E-mail ou senha incorretos. Tente novamente.");
                } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new Exception("Dados inválidos. Verifique as informações e tente novamente.");
                } else {
                    throw new Exception("Erro ao fazer login. Tente novamente mais tarde.");
                }
            }

        } catch (Exception e) {
            log.error("Erro inesperado ao fazer login", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }
}
