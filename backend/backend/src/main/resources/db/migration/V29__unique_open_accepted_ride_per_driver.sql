create unique index if not exists ux_rides_driver_open_accepted
    on rides(driver_id)
    where status = 'ACCEPTED'
    and ended_at is null
    and canceled = false;
