alter table if exists favorite_routes
alter column start_address type text;

alter table if exists favorite_routes
alter column destination_address type text;

alter table if exists favorite_route_stops
alter column address type text;
