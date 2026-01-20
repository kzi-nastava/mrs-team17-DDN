-- V5__seed_ride_tracking_coords.sql

-- Ride 1 (ACTIVE) - primer koordinate Novi Sad
update rides
set
    start_lat = 45.2466, start_lng = 19.8519,   -- FTN (približno)
    dest_lat  = 45.2587, dest_lng  = 19.8426,   -- Železnička (približno)
    car_lat   = 45.2520, car_lng   = 19.8480    -- auto između
where id = 1;

-- Ride 2 (CANCELLED) - primer
update rides
set
    start_lat = 45.2550, start_lng = 19.8330,
    dest_lat  = 45.2440, dest_lng  = 19.8420,
    car_lat   = 45.2525, car_lng   = 19.8385
where id = 2;
