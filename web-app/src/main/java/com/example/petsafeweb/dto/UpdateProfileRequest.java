package com.example.petsafeweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de atualização de perfil (nome e telefone)
 * O mapeamento snake_case é feito automaticamente pelo ObjectMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String fullName;
    private String phone;
}
