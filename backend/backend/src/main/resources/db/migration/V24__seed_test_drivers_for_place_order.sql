do $$
declare
pwd text := '{bcrypt}$2a$10$6eRoyDurT4oPDHRT4LNw0O08W6gvNeuTmd.LejTmwXcukoROk7vIC';
begin

insert into users (role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values ('DRIVER', 'seed-driver1@taxi.app', pwd, 'Seed', 'Driver1', 'Novi Sad', '+38160009001', true, false)
    on conflict (email) do update
                               set role = excluded.role,
                               password_hash = excluded.password_hash,
                               first_name = excluded.first_name,
                               last_name = excluded.last_name,
                               address = excluded.address,
                               phone = excluded.phone,
                               is_active = true,
                               blocked = false,
                               updated_at = now();

insert into drivers (user_id, available, rating)
select u.id, true, 4.80
from users u
where u.email = 'seed-driver1@taxi.app'
    on conflict (user_id) do update
                                 set available = true,
                                 rating = excluded.rating;

insert into vehicles (driver_id, latitude, longitude, model, type, license_plate, seats, baby_transport, pet_transport)
select d.id, 45.2682, 19.8335, 'Skoda Octavia', 'standard', 'NS-901-AA', 4, true, false
from drivers d
         join users u on u.id = d.user_id
where u.email = 'seed-driver1@taxi.app'
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

insert into users (role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values ('DRIVER', 'seed-driver2@taxi.app', pwd, 'Seed', 'Driver2', 'Novi Sad', '+38160009002', true, false)
    on conflict (email) do update
                               set role = excluded.role,
                               password_hash = excluded.password_hash,
                               first_name = excluded.first_name,
                               last_name = excluded.last_name,
                               address = excluded.address,
                               phone = excluded.phone,
                               is_active = true,
                               blocked = false,
                               updated_at = now();

insert into drivers (user_id, available, rating)
select u.id, true, 4.65
from users u
where u.email = 'seed-driver2@taxi.app'
    on conflict (user_id) do update
                                 set available = true,
                                 rating = excluded.rating;

insert into vehicles (driver_id, latitude, longitude, model, type, license_plate, seats, baby_transport, pet_transport)
select d.id, 45.2600, 19.8400, 'Audi A6', 'luxury', 'NS-902-BB', 4, false, true
from drivers d
         join users u on u.id = d.user_id
where u.email = 'seed-driver2@taxi.app'
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

insert into users (role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values ('DRIVER', 'seed-driver3@taxi.app', pwd, 'Seed', 'Driver3', 'Novi Sad', '+38160009003', true, false)
    on conflict (email) do update
                               set role = excluded.role,
                               password_hash = excluded.password_hash,
                               first_name = excluded.first_name,
                               last_name = excluded.last_name,
                               address = excluded.address,
                               phone = excluded.phone,
                               is_active = true,
                               blocked = false,
                               updated_at = now();

insert into drivers (user_id, available, rating)
select u.id, true, 4.55
from users u
where u.email = 'seed-driver3@taxi.app'
    on conflict (user_id) do update
                                 set available = true,
                                 rating = excluded.rating;

insert into vehicles (driver_id, latitude, longitude, model, type, license_plate, seats, baby_transport, pet_transport)
select d.id, 45.2550, 19.8450, 'VW Transporter', 'van', 'NS-903-CC', 6, true, true
from drivers d
         join users u on u.id = d.user_id
where u.email = 'seed-driver3@taxi.app'
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

end $$;
