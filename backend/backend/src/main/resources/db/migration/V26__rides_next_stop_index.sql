alter table rides
    add column if not exists next_stop_index integer not null default 0;
