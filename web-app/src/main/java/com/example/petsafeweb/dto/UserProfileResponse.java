package com.example.petsafeweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para resposta com dados do perfil do usuário
 * O mapeamento snake_case é feito automaticamente pelo ObjectMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String createdAt;
    private String updatedAt;
    private List<PetResponse> pets;
}
