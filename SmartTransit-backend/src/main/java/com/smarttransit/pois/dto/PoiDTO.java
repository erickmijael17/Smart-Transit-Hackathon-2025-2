package com.smarttransit.pois.dto;

import com.smarttransit.pois.model.PoiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoiDTO {
    private String id;
    private PoiType type;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String phone;
    private String website;
    private String openingHours;
    private Map<String, String> tags;
    private Double distance; // en metros
}