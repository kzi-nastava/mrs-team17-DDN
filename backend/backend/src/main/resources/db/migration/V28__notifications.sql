create table if not exists notifications (
                                             id          bigserial primary key,
                                             user_id     bigint not null references users(id) on delete cascade,

    type        varchar(40) not null,
    title       varchar(200) not null,
    message     text not null,

    link_url    text,
    created_at  timestamptz not null default now(),
    read_at     timestamptz
    );

create index if not exists idx_notifications_user_created
    on notifications(user_id, created_at desc);

create index if not exists idx_notifications_user_unread
    on notifications(user_id)
    where read_at is null;
