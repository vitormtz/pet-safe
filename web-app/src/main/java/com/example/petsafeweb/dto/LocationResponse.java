package com.example.petsafeweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * DTO para resposta de localização (models.Location)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private Long id;

    @JsonProperty("device_id")
    private Long deviceId;

    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private Float speed;
    private Float heading;

    @JsonProperty("updated_at")
    private Long updatedAt; // int64 no Go

    @JsonProperty("received_at")
    private Instant receivedAt;
}