package com.example.petsafeweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de pet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long id;
    private String name;
    private String species;
    private String breed;

    @JsonProperty("microchip_id")
    private String microchipId;

    private String dob;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("device_id")
    private Long deviceId;
}
