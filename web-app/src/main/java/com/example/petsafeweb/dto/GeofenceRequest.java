package com.example.petsafeweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceRequest {
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer radius_m;
}
