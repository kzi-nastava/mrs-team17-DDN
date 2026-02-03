-- V10__chat.sql

create table if not exists chat_threads (
                                            id              bigserial primary key,
                                            user_id         bigint not null unique references users(id) on delete cascade,
    created_at      timestamptz not null default now(),
    last_message_at timestamptz
    );

create index if not exists idx_chat_threads_last_message_at
    on chat_threads(last_message_at desc nulls last);

create table if not exists chat_messages (
                                             id              bigserial primary key,
                                             thread_id       bigint not null references chat_threads(id) on delete cascade,
    sender_user_id  bigint references users(id) on delete set null,
    sender_role     varchar(20) not null,
    content         text not null,
    sent_at         timestamptz not null default now()
    );

create index if not exists idx_chat_messages_thread_id_id
    on chat_messages(thread_id, id);

alter table chat_messages
    add constraint chat_messages_sender_role_chk
        check (sender_role in ('PASSENGER','DRIVER','ADMIN'));
