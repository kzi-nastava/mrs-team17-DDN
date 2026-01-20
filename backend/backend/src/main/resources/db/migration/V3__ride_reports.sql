create table if not exists ride_reports (
                                            id          bigserial primary key,
                                            ride_id     bigint not null references rides(id) on delete cascade,
    description text not null,
    created_at  timestamptz not null default now()
    );

create index if not exists idx_ride_reports_ride_id on ride_reports(ride_id);
create index if not exists idx_ride_reports_created_at on ride_reports(created_at);
