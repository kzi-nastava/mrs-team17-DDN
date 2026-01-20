-- V8__schema.sql
-- USERS + linking DRIVERS -> USERS

-- USERS (passenger / driver / admin)
create table if not exists users (
                                     id              bigserial primary key,
                                     role            varchar(20) not null,
    email           varchar(255) not null unique,
    password_hash   varchar(255) not null,
    first_name      varchar(100) not null,
    last_name       varchar(100) not null,
    address         text,
    phone           varchar(50),
    is_active       boolean not null default true,
    blocked         boolean not null default false,
    block_reason    text,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),

    constraint users_role_chk check (role in ('PASSENGER', 'DRIVER', 'ADMIN'))
    );

create index if not exists idx_users_role      on users(role);
create index if not exists idx_users_blocked   on users(blocked);
create index if not exists idx_users_active    on users(is_active);

-- Optional: keep updated_at fresh if you later add triggers in app layer; for now it’s just a column.

-- Link existing DRIVERS table to USERS.
-- IMPORTANT: keep user_id nullable for now because V2__seed inserts driver without user_id.
alter table drivers
    add constraint fk_drivers_user
        foreign key (user_id) references users(id)
            on delete restrict;

-- Helpful index for lookups
create index if not exists idx_drivers_user_id on drivers(user_id);
