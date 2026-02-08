-- Seed additional rides for showcasing history, filtering, details, reports and ratings.
-- Inserts rides for up to 3 driver accounts (prioritizing common seeded accounts).

drop table if exists tmp_s2_rides_data;

create temporary table tmp_s2_rides_data (
    ride_id               bigint not null,
    driver_id             bigint not null,
    driver_rank           int not null,
    seed_order            int not null,
    seed_code             text not null,
    started_at            timestamptz not null,
    ended_at              timestamptz,
    start_address         text not null,
    destination_address   text not null,
    canceled              boolean not null,
    canceled_by           varchar(30),
    status                varchar(30) not null,
    price                 numeric(10,2) not null,
    panic_triggered       boolean not null,
    start_lat             double precision,
    start_lng             double precision,
    dest_lat              double precision,
    dest_lng              double precision,
    picked_up             boolean not null,
    vehicle_type          varchar(20),
    baby_transport        boolean not null,
    pet_transport         boolean not null,
    required_seats        int not null,
    est_distance_meters   double precision,
    est_duration_seconds  double precision
) on commit drop;

with ranked_drivers as (
    select
        d.id as driver_id,
        row_number() over (
            order by
                case lower(coalesce(u.email, ''))
                    when 'driver@test.com' then 1
                    when 'driver1@taxi.app' then 2
                    when 'seed-driver1@taxi.app' then 3
                    when 'driver2@taxi.app' then 4
                    else 100
                end,
                d.id
        ) as driver_rank
    from drivers d
    left join users u on u.id = d.user_id
    where coalesce(u.role, 'DRIVER') = 'DRIVER'
),
target_drivers as (
    select driver_id, driver_rank
    from ranked_drivers
    where driver_rank <= 3
),
ride_seed (
    seed_order,
    seed_code,
    days_ago,
    duration_min,
    status,
    canceled,
    canceled_by,
    price,
    panic_triggered,
    start_address,
    destination_address,
    start_lat,
    start_lng,
    dest_lat,
    dest_lng,
    vehicle_type,
    baby_transport,
    pet_transport,
    required_seats,
    est_distance_meters
) as (
    values
        (1, 'A', 140, 24, 'COMPLETED', false, null::varchar(30),  680.00::numeric(10,2), false, 'Bulevar Evrope 12',       'SPENS, Novi Sad',               45.2576, 19.8219, 45.2464, 19.8486, 'standard', false, false, 1,  6400.0),
        (2, 'B', 110, 31, 'COMPLETED', false, null::varchar(30), 1230.00::numeric(10,2), true,  'Telep centar',           'Železnička stanica Novi Sad',  45.2479, 19.8156, 45.2644, 19.8326, 'luxury',   false, true,  2,  9800.0),
        (3, 'C',  85, 10, 'CANCELLED', true,  'PASSENGER',         360.00::numeric(10,2), false, 'Liman 2, Balzakova',     'Futoška pijaca',               45.2451, 19.8423, 45.2553, 19.8339, 'standard', true,  false, 1,  2800.0),
        (4, 'D',  60, 42, 'COMPLETED', false, null::varchar(30),  890.00::numeric(10,2), false, 'Detelinara, Rumenačka',  'Promenada',                    45.2646, 19.8128, 45.2418, 19.8420, 'van',      true,  true,  4, 11200.0),
        (5, 'E',  40, 14, 'CANCELLED', true,  'DRIVER',            210.00::numeric(10,2), false, 'Podbara, Kosovska',      'Novo naselje, Bate Brkića',    45.2621, 19.8474, 45.2572, 19.8048, 'standard', false, false, 1,  3100.0),
        (6, 'F',  20, 28, 'COMPLETED', false, null::varchar(30), 1040.00::numeric(10,2), false, 'Kej žrtava racije',      'Futog centar',                 45.2518, 19.8524, 45.2389, 19.7161, 'van',      false, true,  3, 16800.0),
        (7, 'G',   7, 19, 'COMPLETED', false, null::varchar(30),  560.00::numeric(10,2), false, 'Adice, Branka Ćopića',   'Klinički centar Vojvodine',    45.2414, 19.7852, 45.2477, 19.8328, 'standard', true,  false, 1,  5200.0),
        (8, 'H',   1, 22, 'COMPLETED', false, null::varchar(30),  780.00::numeric(10,2), false, 'Grbavica, Braće Ribnikar','BIG Shopping Center',          45.2483, 19.8358, 45.2673, 19.8428, 'luxury',   false, true,  2,  7300.0)
)
insert into tmp_s2_rides_data (
    ride_id,
    driver_id,
    driver_rank,
    seed_order,
    seed_code,
    started_at,
    ended_at,
    start_address,
    destination_address,
    canceled,
    canceled_by,
    status,
    price,
    panic_triggered,
    start_lat,
    start_lng,
    dest_lat,
    dest_lng,
    picked_up,
    vehicle_type,
    baby_transport,
    pet_transport,
    required_seats,
    est_distance_meters,
    est_duration_seconds
)
select
    nextval(pg_get_serial_sequence('rides', 'id')) as ride_id,
    td.driver_id,
    td.driver_rank,
    rs.seed_order,
    rs.seed_code,
    (
        date_trunc('day', now())
        - (rs.days_ago * interval '1 day')
        + (td.driver_rank * interval '1 hour')
        + (rs.seed_order * interval '7 minute')
    ) as started_at,
    (
        date_trunc('day', now())
        - (rs.days_ago * interval '1 day')
        + (td.driver_rank * interval '1 hour')
        + (rs.seed_order * interval '7 minute')
        + (rs.duration_min * interval '1 minute')
    ) as ended_at,
    rs.start_address as start_address,
    rs.destination_address,
    rs.canceled,
    rs.canceled_by,
    rs.status,
    rs.price,
    rs.panic_triggered,
    rs.start_lat,
    rs.start_lng,
    rs.dest_lat,
    rs.dest_lng,
    (rs.status = 'COMPLETED') as picked_up,
    rs.vehicle_type,
    rs.baby_transport,
    rs.pet_transport,
    rs.required_seats,
    rs.est_distance_meters,
    (rs.duration_min * 60)::double precision as est_duration_seconds
from target_drivers td
cross join ride_seed rs;

insert into rides (
    id,
    driver_id,
    started_at,
    ended_at,
    start_address,
    destination_address,
    canceled,
    canceled_by,
    status,
    price,
    panic_triggered,
    start_lat,
    start_lng,
    dest_lat,
    dest_lng,
    car_lat,
    car_lng,
    picked_up,
    vehicle_type,
    baby_transport,
    pet_transport,
    required_seats,
    est_distance_meters,
    est_duration_seconds,
    next_stop_index
)
select
    ride_id,
    driver_id,
    started_at,
    ended_at,
    start_address,
    destination_address,
    canceled,
    canceled_by,
    status,
    price,
    panic_triggered,
    start_lat,
    start_lng,
    dest_lat,
    dest_lng,
    case when status = 'COMPLETED' then dest_lat else start_lat end as car_lat,
    case when status = 'COMPLETED' then dest_lng else start_lng end as car_lng,
    picked_up,
    vehicle_type,
    baby_transport,
    pet_transport,
    required_seats,
    est_distance_meters,
    est_duration_seconds,
    0
from tmp_s2_rides_data;

with passenger_seed (seed_order, name, email) as (
    values
        (1, 'Petar Petrović',     'petar@email.com'),
        (2, 'Marko Marković',     'marko@email.com'),
        (3, 'Jovan Jovanović',    'jovan@email.com'),
        (4, 'Ana Anić',           'ana@email.com'),
        (4, 'Petar Petrović',     'petar@email.com'),
        (5, 'Test Passenger',     'passenger@test.com'),
        (6, 'Marko Marković',     'marko@email.com'),
        (6, 'Jovan Jovanović',    'jovan@email.com'),
        (7, 'Ana Anić',           'ana@email.com'),
        (8, 'Petar Petrović',     'petar@email.com'),
        (8, 'Marko Marković',     'marko@email.com')
)
insert into ride_passengers (ride_id, name, email)
select
    r.ride_id,
    p.name,
    p.email
from tmp_s2_rides_data r
join passenger_seed p on p.seed_order = r.seed_order;

with stop_seed (seed_order, stop_order, address, lat, lng) as (
    values
        (4, 1, 'Bulevar oslobođenja 102', 45.2548, 19.8366),
        (4, 2, 'Liman, Narodnog fronta 23', 45.2378, 19.8339),
        (6, 1, 'Veternik, Cara Lazara 5', 45.2507, 19.7582),
        (8, 1, 'Bulevar cara Lazara 78', 45.2442, 19.8401),
        (8, 2, 'Bulevar patrijarha Pavla 17', 45.2587, 19.8098)
)
insert into ride_stops (ride_id, stop_order, address, lat, lng)
select
    r.ride_id,
    s.stop_order,
    s.address,
    s.lat,
    s.lng
from tmp_s2_rides_data r
join stop_seed s on s.seed_order = r.seed_order;

with report_seed (seed_order, minutes_after_start, description) as (
    values
        (2, 12, 'Passenger reported route deviation near city center.'),
        (3,  6, 'Ride canceled by passenger due to long wait time.'),
        (5,  5, 'Driver canceled ride due to technical issue.'),
        (6, 15, 'Passenger requested verification of route choice.'),
        (8, 10, 'Minor traffic delay reported by passenger.')
)
insert into ride_reports (ride_id, description, created_at)
select
    r.ride_id,
    rep.description,
    r.started_at + (rep.minutes_after_start * interval '1 minute')
from tmp_s2_rides_data r
join report_seed rep on rep.seed_order = r.seed_order;

with rating_seed (seed_order, driver_rating, vehicle_rating, comment) as (
    values
        (1, 5, 5, 'Excellent ride, very professional driver.'),
        (2, 4, 4, 'Good service, slight delay due to traffic.'),
        (4, 5, 4, 'Comfortable ride with multiple stops handled well.'),
        (6, 4, 5, 'Driver was polite and the vehicle was clean.')
)
insert into ride_ratings (ride_id, driver_rating, vehicle_rating, comment, created_at)
select
    r.ride_id,
    rr.driver_rating,
    rr.vehicle_rating,
    rr.comment,
    coalesce(r.ended_at, r.started_at) + interval '10 minute'
from tmp_s2_rides_data r
join rating_seed rr on rr.seed_order = r.seed_order
where r.status = 'COMPLETED';
