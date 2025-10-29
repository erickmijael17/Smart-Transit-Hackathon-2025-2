package com.smarttransit.bus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusDTO {
    private String id;
    private String plate;
    private String routeId;
    private Integer capacity;
    private String status; // "active", "maintenance", "inactive"
    private String assignedDriver;
}


