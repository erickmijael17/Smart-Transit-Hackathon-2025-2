package com.smarttransit.tracking.mapper;

import com.smarttransit.tracking.dto.BusPositionDTO;
import com.smarttransit.tracking.entity.BusPosition;
import com.smarttransit.tracking.entity.CurrentBusState;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BusPositionMapper {
    @Mapping(target = "timestamp", ignore = true) // lo rellena @CreationTimestamp
    @Mapping(target = "estimatedArrivalTime", expression = "java(toOffsetDateTime(dto.getEstimatedArrivalTime()))")
    BusPosition toEntity(BusPositionDTO dto);

    @Mapping(target = "timestamp", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "estimatedArrivalTime", expression = "java(toOffsetDateTime(dto.getEstimatedArrivalTime()))")
    CurrentBusState toCurrentState(BusPositionDTO dto);
    
    default java.time.OffsetDateTime toOffsetDateTime(java.time.LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(java.time.ZoneOffset.UTC) : null;
    }
}


