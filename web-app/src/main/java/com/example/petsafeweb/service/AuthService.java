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
            // Logar apenas se não for erro esperado de validação
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.CONFLICT) {
                log.debug("Tentativa de registro falhou - Status: {}", e.getStatusCode());
            } else {
                log.error("Erro ao registrar usuário. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            }

            // Tentar parsear a resposta de erro
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

            // Se conseguiu parsear e tem mensagem, usar a mensagem da API
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                throw new Exception(errorMessage);
            }

            // Caso contrário, retornar mensagem baseada no status HTTP
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new Exception("Dados inválidos. Verifique as informações e tente novamente.");
            } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new Exception("E-mail já cadastrado. Tente fazer login ou use outro e-mail.");
            } else {
                throw new Exception("Erro ao registrar usuário. Tente novamente mais tarde.");
            }

        } catch (Exception e) {
            // Se a exceção já foi tratada e tem uma mensagem customizada, re-lançar sem logar
            if (e.getMessage() != null &&
                (e.getMessage().contains("Dados inválidos") ||
                 e.getMessage().contains("E-mail já cadastrado") ||
                 e.getMessage().contains("Erro ao registrar usuário"))) {
                throw e;
            }

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
            // Logar apenas se não for erro de autenticação esperado
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.debug("Tentativa de login falhou - credenciais inválidas");
            } else {
                log.error("Erro ao fazer login. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            }

            // Tentar parsear a resposta de erro
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

            // Se conseguiu parsear e tem mensagem, usar a mensagem da API
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                throw new Exception(errorMessage);
            }

            // Caso contrário, retornar mensagem baseada no status HTTP
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new Exception("E-mail ou senha incorretos. Tente novamente.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new Exception("Dados inválidos. Verifique as informações e tente novamente.");
            } else {
                throw new Exception("Erro ao fazer login. Tente novamente mais tarde.");
            }

        } catch (Exception e) {
            // Se a exceção já foi tratada e tem uma mensagem customizada, re-lançar sem logar
            if (e.getMessage() != null &&
                (e.getMessage().contains("E-mail ou senha") ||
                 e.getMessage().contains("Dados inválidos") ||
                 e.getMessage().contains("Erro ao fazer login"))) {
                throw e;
            }

            log.error("Erro inesperado ao fazer login", e);
            throw new Exception("Erro ao conectar com o servidor. Tente novamente mais tarde.");
        }
    }
}
