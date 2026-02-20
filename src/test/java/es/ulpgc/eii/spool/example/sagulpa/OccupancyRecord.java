package es.ulpgc.eii.spool.example.sagulpa;

import java.time.Instant;

public record OccupancyRecord(
        int recordId,
        String parkingLotId,
        int freeSpots,
        Instant recordedAt,
        String status
) {}
