insert into drivers (user_id, available, rating)
select u.id, false, null
from users u
where u.role = 'DRIVER'
  and not exists (
    select 1
    from drivers d
    where d.user_id = u.id
);

insert into vehicles (
    driver_id,
    latitude, longitude,
    model, type, license_plate, seats,
    baby_transport, pet_transport
)
select
    d.id,
    45.2671, 19.8335,
    'Skoda Octavia', 'standard',
    left('DRV-' || d.id::text || '-' || d.user_id::text, 30),
    4,
    false, false
from drivers d
    join users u on u.id = d.user_id
where u.role = 'DRIVER'
  and not exists (
    select 1
    from vehicles v
    where v.driver_id = d.id
    );

do $$
declare
cnt_driver_users  int;
  cnt_drivers       int;
  cnt_vehicles      int;
begin
select count(*) into cnt_driver_users
from users
where role = 'DRIVER';

select count(*) into cnt_drivers
from drivers d
         join users u on u.id = d.user_id
where u.role = 'DRIVER';

select count(*) into cnt_vehicles
from vehicles v
         join drivers d on d.id = v.driver_id
         join users u on u.id = d.user_id
where u.role = 'DRIVER';

raise notice 'After V25: DRIVER users=% | linked drivers=% | vehicles=%',
    cnt_driver_users, cnt_drivers, cnt_vehicles;
end $$;
