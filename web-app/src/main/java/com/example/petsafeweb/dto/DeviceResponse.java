package com.example.petsafeweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para resposta de dispositivo (models.Device)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;

    @JsonProperty("serial_number")
    private String serialNumber;

    private String imei;
    private String model;
    private String firmware;

    @JsonProperty("owner_id")
    private Long ownerId;

    private Boolean active;

    @JsonProperty("last_latitude")
    private Double lastLatitude;

    @JsonProperty("last_longitude")
    private Double lastLongitude;

    @JsonProperty("last_comm")
    private Instant lastComm; // Time em Go geralmente mapeia para Instant em Java

    @JsonProperty("pet_id")
    private Long petId;
}