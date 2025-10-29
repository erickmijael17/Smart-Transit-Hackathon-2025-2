package com.smarttransit.driver.mapper;

import com.smarttransit.driver.entity.Driver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    // reservado para futuras conversiones DTO<->Entity
    Driver copy(Driver source);
}





