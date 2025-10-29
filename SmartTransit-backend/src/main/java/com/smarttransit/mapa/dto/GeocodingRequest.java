package com.smarttransit.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GeocodingRequest {
    @NotBlank(message = "Address cannot be empty")
    private String address;
}