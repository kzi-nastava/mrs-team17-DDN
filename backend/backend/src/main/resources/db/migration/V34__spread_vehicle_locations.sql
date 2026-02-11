with params as (
    select
        45.2671::double precision as base_lat,
    19.8335::double precision as base_lng,
    50::int as cols,
    0.001::double precision as step_lat,
    0.0015::double precision as step_lng
    ),
    ranked as (
select id, row_number() over (order by id) as rn
from vehicles
    ),
    coords as (
select
    r.id,
    p.base_lat + (((r.rn - 1) % p.cols) - (p.cols / 2)) * p.step_lat as lat,
    p.base_lng + (((r.rn - 1) / p.cols) - (p.cols / 2)) * p.step_lng as lng
from ranked r
    cross join params p
    )
update vehicles v
set latitude = c.lat,
    longitude = c.lng,
    updated_at = now()
    from coords c
where v.id = c.id;