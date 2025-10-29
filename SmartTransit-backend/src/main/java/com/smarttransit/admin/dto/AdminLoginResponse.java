package com.smarttransit.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String adminId;
    private String username;
    private String fullName;
    private String role;
    private String token;
}


