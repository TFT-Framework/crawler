package es.ulpgc.eii.spool.example.sagulpa;

import es.ulpgc.eii.spool.domain.EventCategory;
import es.ulpgc.eii.spool.domain.SchemaVersion;
import es.ulpgc.eii.spool.domain.crawler.EventDeserializer;

import java.util.UUID;

public class ParkingEventMapper implements EventDeserializer<OccupancyRecord, ParkingEvent> {
    @Override
    public ParkingEvent deserialize(OccupancyRecord record) {
        return new ParkingEvent(
                UUID.randomUUID().toString(),
                record.parkingLotId(),
                "sagulpa-" + record.recordId(),
                EventCategory.DOMAIN,
                "OCCUPANCY_UPDATED",
                record.recordedAt(),
                SchemaVersion.of("1.0.0"),
                record.parkingLotId(),
                record.freeSpots()
        );
    }
}

