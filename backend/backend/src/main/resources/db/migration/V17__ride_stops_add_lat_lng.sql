alter table ride_stops
    add column if not exists lat double precision,
    add column if not exists lng double precision;

update ride_stops
set lat = 45.2671,
    lng = 19.8335
where lat is null or lng is null;

alter table ride_stops
    alter column lat set not null,
alter column lng set not null;