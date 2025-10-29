package com.smarttransit.tracking.entity;

import com.smarttransit.tracking.model.BusStatus;
import com.smarttransit.tracking.model.OccupancyLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bus_positions", indexes = {
        @Index(name = "idx_bus_positions_bus_time", columnList = "busId,timestamp")
})
@Getter
@Setter
public class BusPosition {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 60)
    private String busId;

    @Column(length = 20)
    private String routeId;

    @Column(length = 120)
    private String routeName;

    private double latitude;
    private double longitude;
    private double bearing;
    private double speed;

    @Enumerated(EnumType.STRING)
    private BusStatus status;

    @CreationTimestamp
    private OffsetDateTime timestamp;

    private int progress;

    @Enumerated(EnumType.STRING)
    private OccupancyLevel occupancyLevel;
    
    // Campos opcionales
    @Column(length = 60)
    private String nextStopId;
    
    @Column(length = 120)
    private String nextStopName;
    
    private OffsetDateTime estimatedArrivalTime;
    
    private Integer distanceToDestination;
    
    private Integer passengerCount;
    
    private Integer delay;
}



