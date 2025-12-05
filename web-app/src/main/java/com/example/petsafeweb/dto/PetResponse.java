package com.example.petsafeweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de pet
 * O mapeamento snake_case é feito automaticamente pelo ObjectMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long id;
    private Long ownerId;
    private String name;
    private String species;
    private String breed;
    private String microchipId;
    private String dob;
    private Long userId;
    private Long deviceId;
    private String createdAt;
}
