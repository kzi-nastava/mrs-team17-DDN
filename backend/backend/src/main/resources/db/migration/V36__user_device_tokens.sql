create table if not exists user_device_tokens (
    id            bigserial primary key,
    user_id       bigint not null references users(id) on delete cascade,
    token         text not null unique,
    platform      varchar(20) not null default 'ANDROID',
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    last_seen_at  timestamptz not null default now()
);

create index if not exists idx_user_device_tokens_user
    on user_device_tokens(user_id);

create index if not exists idx_user_device_tokens_platform
    on user_device_tokens(platform);
