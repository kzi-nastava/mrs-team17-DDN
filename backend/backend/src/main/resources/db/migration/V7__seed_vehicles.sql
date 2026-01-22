insert into vehicles (
    id, driver_id, latitude, longitude,
    model, type, license_plate, seats, baby_transport, pet_transport
)
values
    (1, 1, 45.2671, 19.8335, 'Skoda Octavia', 'standard', 'NS-123-AB', 4, true, false)
    on conflict (driver_id) do update
                                   set latitude = excluded.latitude,
                                   longitude = excluded.longitude,
                                   model = excluded.model,
                                   type = excluded.type,
                                   license_plate = excluded.license_plate,
                                   seats = excluded.seats,
                                   baby_transport = excluded.baby_transport,
                                   pet_transport = excluded.pet_transport,
                                   updated_at = now();
