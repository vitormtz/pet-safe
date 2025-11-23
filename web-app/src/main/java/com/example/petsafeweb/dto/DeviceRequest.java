package com.example.petsafeweb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de criação/atualização de dispositivo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY) // Inclui apenas campos não-vazios/null na requisição (ideal para
                                            // PATCH/Update)
public class DeviceRequest {

    @JsonProperty("serial_number")
    private String serialNumber;

    private String imei;
    private String model;
    private String firmware;

    @JsonProperty("pet_id")
    private Long petId; // pet_id é *uint64 no Go, Long em Java é o tipo mais próximo

    private String connectivity;
    private Boolean active;
}