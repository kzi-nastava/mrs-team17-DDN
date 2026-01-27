-- V4__ride_tracking_coords.sql

alter table rides
    add column if not exists start_lat double precision,
    add column if not exists start_lng double precision,
    add column if not exists dest_lat  double precision,
    add column if not exists dest_lng  double precision,
    add column if not exists car_lat   double precision,
    add column if not exists car_lng   double precision;

create index if not exists idx_rides_start_lat_lng on rides(start_lat, start_lng);
create index if not exists idx_rides_dest_lat_lng  on rides(dest_lat, dest_lng);
