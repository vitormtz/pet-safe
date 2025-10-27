package com.example.petsafeweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO wrapper para resposta da lista de pets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetsListResponse {

    @JsonProperty("data")
    private List<PetResponse> data;
}
