-- V1__schema.sql

-- DRIVERS
create table if not exists drivers (
                                       id          bigserial primary key,
                                       user_id     bigint unique, -- later: references users(id) + set NOT NULL when users exist
                                       available   boolean not null default false,
                                       rating      numeric(3,2),
    created_at  timestamptz not null default now()
    );

create index if not exists idx_drivers_available on drivers(available);

-- RIDES (final schema used by DTOs)
create table if not exists rides (
                                     id                  bigserial primary key,
                                     driver_id            bigint not null references drivers(id) on delete restrict,

    started_at           timestamptz,
    ended_at             timestamptz,

    start_address        text not null,
    destination_address  text not null,

    canceled             boolean not null default false,
    canceled_by          varchar(30),
    status               varchar(30) not null default 'REQUESTED',

    price                numeric(10,2) not null default 0,
    panic_triggered      boolean not null default false,

    created_at           timestamptz not null default now(),

    constraint rides_status_chk check (status in (
                                       'REQUESTED', 'ACCEPTED', 'REJECTED', 'CANCELLED',
                                       'ACTIVE', 'COMPLETED'
                                                 ))
    );

create index if not exists idx_rides_driver_id   on rides(driver_id);
create index if not exists idx_rides_started_at  on rides(started_at);
create index if not exists idx_rides_status      on rides(status);

-- RIDE STOPS
create table if not exists ride_stops (
                                          id          bigserial primary key,
                                          ride_id     bigint not null references rides(id) on delete cascade,
    stop_order  int not null,
    address     text not null,
    constraint ride_stops_unique_order unique (ride_id, stop_order)
    );

create index if not exists idx_ride_stops_ride_id on ride_stops(ride_id);

-- RIDE PASSENGERS (stored as name/email for now)
create table if not exists ride_passengers (
                                               id          bigserial primary key,
                                               ride_id     bigint not null references rides(id) on delete cascade,
    name        text not null,
    email       text not null
    );

create index if not exists idx_ride_passengers_ride_id on ride_passengers(ride_id);
