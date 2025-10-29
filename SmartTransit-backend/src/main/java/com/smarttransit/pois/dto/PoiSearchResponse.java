package com.smarttransit.pois.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PoiSearchResponse {
    private List<PoiDTO> pois;
    private int count;
    private long searchTimeMs;
}