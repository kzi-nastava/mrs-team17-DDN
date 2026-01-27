insert into favorite_routes
(id, user_id, name, start_address, start_lat, start_lng, destination_address, dest_lat, dest_lng)
values
    (9001, 3003, 'Bulevar → Centar',
     'Bulevar Oslobodjenja 1, Novi Sad', 45.25590, 19.83395,
     'Trg Slobode 1, Novi Sad',          45.25625, 19.84540)
    on conflict (id) do nothing;

insert into favorite_route_stops
(favorite_route_id, stop_order, address, lat, lng)
values
    (9001, 1, 'Zmaj Jovina 10, Novi Sad', 45.25685, 19.84430),
    (9001, 2, 'Dunavska 5, Novi Sad',     45.25730, 19.84600)
    on conflict (favorite_route_id, stop_order) do nothing;

select setval(
               pg_get_serial_sequence('favorite_routes', 'id'),
               (select coalesce(max(id), 1) from favorite_routes)
       );
