package com.smarttransit.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverLoginResponse {
    private String driverId;
    private String driverName;
    private String busId;
    private String routeId;
    private boolean authenticated;
    private String token;
}





