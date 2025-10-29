package com.smarttransit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartTransitApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTransitApplication.class, args);
    }

}
