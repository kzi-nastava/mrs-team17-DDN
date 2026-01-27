-- V9__seed.sql
-- Seed users (admins, drivers, passengers) + connect existing driver(id=1) to a driver-user.

-- ========== ADMINS ==========
insert into users (id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values
    (1001, 'ADMIN', 'admin1@taxi.app', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Admin', 'One', 'Novi Sad', '+38160000001', true, false),
    (1002, 'ADMIN', 'admin2@taxi.app', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Admin', 'Two', 'Novi Sad', '+38160000002', true, false)
    on conflict (id) do nothing;

-- ========== DRIVERS (as users) ==========
insert into users (id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values
    (2001, 'DRIVER', 'driver1@taxi.app', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Petar', 'Petrovic', 'Novi Sad', '+38160001001', true, false),
    (2002, 'DRIVER', 'driver2@taxi.app', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Milan', 'Milic',    'Novi Sad', '+38160001002', true, false),
    (2003, 'DRIVER', 'driver3@taxi.app', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Nikola','Nikolic',  'Novi Sad', '+38160001003', true, false)
    on conflict (id) do nothing;

-- ========== PASSENGERS ==========
insert into users (id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values
    (3001, 'PASSENGER', 'petar@email.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Petar', 'Petrovic',   'Novi Sad', '+38160002001', true, false),
    (3002, 'PASSENGER', 'marko@email.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Marko', 'Markovic',   'Novi Sad', '+38160002002', true, false),
    (3003, 'PASSENGER', 'jovan@email.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Jovan', 'Jovanovic',  'Novi Sad', '+38160002003', true, false),
    (3004, 'PASSENGER', 'ana@email.com',   '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'Ana',   'Anic',       'Novi Sad', '+38160002004', true, false)
    on conflict (id) do nothing;

-- ========== CONNECT DRIVERS TABLE TO USERS ==========
-- 1) Patch existing driver from V2 (id=1) to map to driver user (id=2001)
update drivers
set user_id = 2001
where id = 1 and user_id is null;

-- 2) Add a few more driver rows (so you have multiple drivers in DB)
insert into drivers (id, user_id, available, rating)
values
    (2, 2002, true,  4.75),
    (3, 2003, false, 4.60)
    on conflict (id) do nothing;

-- If you want: ensure user_id is unique already in drivers table by schema.
