package com.smarttransit.driver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {
    private String id;
    private String username;
    private String displayName;
    private String busId;
    private String routeId;
    private String status; // "active", "inactive"
    private String phone;
    private String email;
}


