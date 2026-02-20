INSERT INTO parking_lot VALUES
                            ('elder',      'Parking Elder',      280, 'centro'),
                            ('metropol',   'Parking Metropol',   350, 'centro'),
                            ('guanarteme', 'Parking Guanarteme', 190, 'guanarteme'),
                            ('puerto',     'Parking Puerto',     420, 'puerto');

INSERT INTO occupancy (parking_lot_id, free_spots, recorded_at, status) VALUES
                                                                            ('elder',       150, NOW() - INTERVAL '10 minutes', 'OPEN'),
                                                                            ('elder',       120, NOW() - INTERVAL '5 minutes',  'OPEN'),
                                                                            ('metropol',    200, NOW() - INTERVAL '8 minutes',  'OPEN'),
                                                                            ('metropol',      0, NOW() - INTERVAL '2 minutes',  'FULL'),
                                                                            ('guanarteme',   90, NOW() - INTERVAL '6 minutes',  'OPEN'),
                                                                            ('puerto',      300, NOW(),                          'OPEN');