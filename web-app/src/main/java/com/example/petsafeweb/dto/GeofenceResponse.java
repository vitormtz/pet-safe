package com.example.petsafeweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceResponse {
    private Long id;
    private Long owner_id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer radius_m;
    private Boolean active;
    private String created_at;
}
