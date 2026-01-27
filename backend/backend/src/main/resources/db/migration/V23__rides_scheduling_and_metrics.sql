alter table rides
    add column if not exists scheduled_at timestamptz,
    add column if not exists est_distance_meters double precision,
    add column if not exists est_duration_seconds double precision;

create index if not exists idx_rides_scheduled_at on rides(scheduled_at);
