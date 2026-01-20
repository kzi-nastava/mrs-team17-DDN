insert into vehicles (id, driver_id, latitude, longitude)
values
    (1, 1, 45.2671, 19.8335)
    on conflict (driver_id) do update
                                   set latitude = excluded.latitude,
                                   longitude = excluded.longitude,
                                   updated_at = now();
