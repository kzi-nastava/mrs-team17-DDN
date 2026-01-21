create table if not exists driver_profile_change_requests (
                                                              id               bigserial primary key,
                                                              driver_id         bigint not null references drivers(id) on delete restrict,

    first_name        varchar(100),
    last_name         varchar(100),
    address           text,
    phone             varchar(50),
    profile_image_url text,

    status            varchar(20) not null default 'PENDING',
    created_at        timestamptz not null default now(),

    decided_at        timestamptz,
    decided_by        bigint,
    note              text,

    constraint dpr_status_chk check (status in ('PENDING','APPROVED','REJECTED'))
    );

create index if not exists idx_dpr_driver_id on driver_profile_change_requests(driver_id);
create index if not exists idx_dpr_status on driver_profile_change_requests(status);
create index if not exists idx_dpr_created_at on driver_profile_change_requests(created_at);
