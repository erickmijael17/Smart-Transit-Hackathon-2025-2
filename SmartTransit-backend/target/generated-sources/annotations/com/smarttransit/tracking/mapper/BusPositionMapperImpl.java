package com.smarttransit.tracking.mapper;

import com.smarttransit.tracking.dto.BusPositionDTO;
import com.smarttransit.tracking.entity.BusPosition;
import com.smarttransit.tracking.entity.CurrentBusState;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-29T13:26:55-0500",
    comments = "version: 1.6.0, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class BusPositionMapperImpl implements BusPositionMapper {

    @Override
    public BusPosition toEntity(BusPositionDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BusPosition busPosition = new BusPosition();

        busPosition.setBusId( dto.getBusId() );
        busPosition.setRouteId( dto.getRouteId() );
        busPosition.setRouteName( dto.getRouteName() );
        busPosition.setLatitude( dto.getLatitude() );
        busPosition.setLongitude( dto.getLongitude() );
        busPosition.setBearing( dto.getBearing() );
        busPosition.setSpeed( dto.getSpeed() );
        busPosition.setStatus( dto.getStatus() );
        busPosition.setProgress( dto.getProgress() );
        busPosition.setOccupancyLevel( dto.getOccupancyLevel() );
        busPosition.setNextStopId( dto.getNextStopId() );
        busPosition.setNextStopName( dto.getNextStopName() );
        busPosition.setDistanceToDestination( dto.getDistanceToDestination() );
        busPosition.setPassengerCount( dto.getPassengerCount() );
        busPosition.setDelay( dto.getDelay() );

        busPosition.setEstimatedArrivalTime( toOffsetDateTime(dto.getEstimatedArrivalTime()) );

        return busPosition;
    }

    @Override
    public CurrentBusState toCurrentState(BusPositionDTO dto) {
        if ( dto == null ) {
            return null;
        }

        CurrentBusState currentBusState = new CurrentBusState();

        currentBusState.setBusId( dto.getBusId() );
        currentBusState.setRouteId( dto.getRouteId() );
        currentBusState.setRouteName( dto.getRouteName() );
        currentBusState.setLatitude( dto.getLatitude() );
        currentBusState.setLongitude( dto.getLongitude() );
        currentBusState.setBearing( dto.getBearing() );
        currentBusState.setSpeed( dto.getSpeed() );
        currentBusState.setStatus( dto.getStatus() );
        currentBusState.setProgress( dto.getProgress() );
        currentBusState.setOccupancyLevel( dto.getOccupancyLevel() );
        currentBusState.setNextStopId( dto.getNextStopId() );
        currentBusState.setNextStopName( dto.getNextStopName() );
        currentBusState.setDistanceToDestination( dto.getDistanceToDestination() );
        currentBusState.setPassengerCount( dto.getPassengerCount() );
        currentBusState.setDelay( dto.getDelay() );

        currentBusState.setTimestamp( java.time.OffsetDateTime.now() );
        currentBusState.setEstimatedArrivalTime( toOffsetDateTime(dto.getEstimatedArrivalTime()) );

        return currentBusState;
    }
}
