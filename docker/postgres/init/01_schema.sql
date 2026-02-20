CREATE TABLE parking_lot (
                             parking_lot_id  VARCHAR(20) PRIMARY KEY,
                             name            VARCHAR(100) NOT NULL,
                             total_capacity  INTEGER NOT NULL,
                             zone            VARCHAR(50)
);

CREATE TABLE occupancy (
                           record_id       SERIAL PRIMARY KEY,
                           parking_lot_id  VARCHAR(20) REFERENCES parking_lot(parking_lot_id),
                           free_spots      INTEGER NOT NULL,
                           recorded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
                           status          VARCHAR(20) NOT NULL
);