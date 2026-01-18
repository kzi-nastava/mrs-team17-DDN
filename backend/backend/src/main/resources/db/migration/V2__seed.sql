-- V2__seed.sql

insert into drivers (id, available, rating)
values (1, true, 4.90)
    on conflict (id) do nothing;

insert into rides
(id, driver_id, started_at, ended_at, start_address, destination_address, canceled, canceled_by, status, price, panic_triggered)
values
    (1, 1, '2025-12-13T14:30:00+01:00', null, 'FTN', 'Železnička', false, null, 'ACTIVE', 820, false),
    (2, 1, '2025-12-12T09:10:00+01:00', '2025-12-12T09:35:00+01:00', 'Bulevar oslobođenja', 'Limanski park', true, 'PASSENGER', 'CANCELLED', 540, false)
    on conflict (id) do nothing;

insert into ride_stops (ride_id, stop_order, address)
values (2, 1, 'Spens')
    on conflict (ride_id, stop_order) do nothing;

insert into ride_passengers (ride_id, name, email) values
                                                       (1, 'Petar Petrović', 'petar@email.com'),
                                                       (1, 'Marko Marković', 'marko@email.com'),
                                                       (2, 'Jovan Jovanović', 'jovan@email.com');
