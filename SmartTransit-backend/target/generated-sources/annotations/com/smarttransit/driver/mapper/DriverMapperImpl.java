package com.smarttransit.driver.mapper;

import com.smarttransit.driver.entity.Driver;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-29T13:26:55-0500",
    comments = "version: 1.6.0, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class DriverMapperImpl implements DriverMapper {

    @Override
    public Driver copy(Driver source) {
        if ( source == null ) {
            return null;
        }

        Driver driver = new Driver();

        driver.setId( source.getId() );
        driver.setUsername( source.getUsername() );
        driver.setDisplayName( source.getDisplayName() );
        driver.setPasswordHash( source.getPasswordHash() );
        driver.setActive( source.getActive() );
        driver.setBusId( source.getBusId() );
        driver.setRouteId( source.getRouteId() );
        driver.setCreatedAt( source.getCreatedAt() );

        return driver;
    }
}
