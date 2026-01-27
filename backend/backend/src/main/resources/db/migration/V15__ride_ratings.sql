create table if not exists ride_ratings (
                                            id             bigserial primary key,
                                            ride_id         bigint not null references rides(id) on delete cascade,

    driver_rating   int not null,
    vehicle_rating  int not null,
    comment         text,

    created_at      timestamptz not null default now(),

    constraint ride_ratings_ride_unique unique (ride_id),
    constraint ride_ratings_driver_chk check (driver_rating between 1 and 5),
    constraint ride_ratings_vehicle_chk check (vehicle_rating between 1 and 5)
    );

create index if not exists idx_ride_ratings_ride_id on ride_ratings(ride_id);
create index if not exists idx_ride_ratings_created_at on ride_ratings(created_at);
