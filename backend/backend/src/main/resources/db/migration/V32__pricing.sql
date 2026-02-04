create table if not exists pricing (
                                       vehicle_type varchar(20) primary key,
    base_price   numeric(10,2) not null,
    updated_at   timestamptz not null default now(),
    constraint pricing_type_chk check (vehicle_type in ('standard','luxury','van')),
    constraint pricing_base_chk check (base_price >= 0)
    );

insert into pricing(vehicle_type, base_price)
select x.vehicle_type, x.base_price
from (values
          ('standard', 200.00),
          ('luxury',   300.00),
          ('van',      250.00)
     ) as x(vehicle_type, base_price)
where not exists (select 1 from pricing p where p.vehicle_type = x.vehicle_type);
