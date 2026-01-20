create table if not exists vehicles (
                                        id         bigserial primary key,
                                        driver_id  bigint not null references drivers(id) on delete restrict,
    latitude   double precision not null,
    longitude  double precision not null,
    updated_at timestamptz not null default now(),
    constraint vehicles_driver_unique unique (driver_id)
    );

create index if not exists idx_vehicles_lat_lng on vehicles(latitude, longitude);
create index if not exists idx_vehicles_driver_id on vehicles(driver_id);
