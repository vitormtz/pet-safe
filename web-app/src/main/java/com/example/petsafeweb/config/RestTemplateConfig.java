package com.example.petsafeweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuração do RestTemplate para fazer requisições HTTP
 * Usa HttpComponentsClientHttpRequestFactory para suportar PATCH
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Usar HttpComponentsClientHttpRequestFactory para suportar todos os métodos HTTP incluindo PATCH
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setConnectionRequestTimeout(5000);

        return new RestTemplate(requestFactory);
    }
}
