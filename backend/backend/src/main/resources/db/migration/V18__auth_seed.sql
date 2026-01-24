-- V9__auth_seed.sql
-- minimal seed users + link driver(1) -> user(driver)

-- USERS
insert into users (id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked)
values
    (1, 'ADMIN',     'admin@test.com',     '{noop}admin',     'Admin', 'Admin', null, null, true, false),
    (2, 'DRIVER',    'driver@test.com',    '{noop}driver',    'Driver', 'One',  null, null, true, false),
    (3, 'PASSENGER', 'passenger@test.com', '{noop}passenger', 'Pera',  'Peric', null, null, true, false)
    on conflict (id) do nothing;

-- ensure email unique too (in case ids differ)
insert into users (role, email, password_hash, first_name, last_name, is_active, blocked)
select 'ADMIN', 'admin@test.com', '{noop}admin', 'Admin', 'Admin', true, false
    where not exists (select 1 from users where email='admin@test.com');

insert into users (role, email, password_hash, first_name, last_name, is_active, blocked)
select 'DRIVER', 'driver@test.com', '{noop}driver', 'Driver', 'One', true, false
    where not exists (select 1 from users where email='driver@test.com');

insert into users (role, email, password_hash, first_name, last_name, is_active, blocked)
select 'PASSENGER', 'passenger@test.com', '{noop}passenger', 'Pera', 'Peric', true, false
    where not exists (select 1 from users where email='passenger@test.com');

-- LINK existing driver id=1 to driver user (email driver@test.com)
update drivers
set user_id = (select id from users where email = 'driver@test.com')
where id = 1 and user_id is null;
