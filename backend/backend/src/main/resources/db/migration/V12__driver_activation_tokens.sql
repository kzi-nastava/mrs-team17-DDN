create table if not exists driver_activation_tokens (
                                                        id          bigserial primary key,
                                                        user_id     bigint not null references users(id) on delete cascade,

    token       varchar(128) not null unique,
    expires_at  timestamptz not null,
    used_at     timestamptz,

    created_at  timestamptz not null default now(),

    constraint dat_used_chk check (used_at is null or used_at >= created_at)
    );

create index if not exists idx_dat_user_id on driver_activation_tokens(user_id);
create index if not exists idx_dat_expires_at on driver_activation_tokens(expires_at);