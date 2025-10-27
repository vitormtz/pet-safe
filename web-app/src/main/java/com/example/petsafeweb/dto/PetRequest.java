package com.example.petsafeweb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de criação/atualização de pet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PetRequest {

    private String name;
    private String species;
    private String breed;

    @JsonProperty("microchip_id")
    private String microchipId;

    private String dob; // Data de nascimento no formato ISO 8601 (yyyy-MM-ddTHH:mm:ssZ)
}
