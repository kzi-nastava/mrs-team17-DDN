create table if not exists vehicles (
                                        id             bigserial primary key,
                                        driver_id       bigint not null references drivers(id) on delete restrict,

    latitude        double precision not null,
    longitude       double precision not null,
    model           varchar(100) not null,
    type            varchar(20)  not null,
    license_plate   varchar(30)  not null,
    seats           int          not null,
    baby_transport  boolean      not null default false,
    pet_transport   boolean      not null default false,

    created_at      timestamptz  not null default now(),
    updated_at      timestamptz  not null default now(),

    constraint vehicles_driver_unique unique (driver_id),
    constraint vehicles_plate_unique  unique (license_plate),
    constraint vehicles_type_chk      check (type in ('standard', 'luxury', 'van')),
    constraint vehicles_seats_chk     check (seats > 0)
    );

create index if not exists idx_vehicles_lat_lng on vehicles(latitude, longitude);
create index if not exists idx_vehicles_driver_id on vehicles(driver_id);
create index if not exists idx_vehicles_plate on vehicles(license_plate);
