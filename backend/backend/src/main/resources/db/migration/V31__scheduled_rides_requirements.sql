alter table rides
    add column if not exists vehicle_type varchar(20);

alter table rides
    add column if not exists baby_transport boolean not null default false;

alter table rides
    add column if not exists pet_transport boolean not null default false;

alter table rides
    add column if not exists required_seats int not null default 1;

alter table rides drop constraint if exists rides_status_chk;

alter table rides add constraint rides_status_chk check (status in (
                                                                    'REQUESTED',
                                                                    'SCHEDULED',
                                                                    'ACCEPTED',
                                                                    'REJECTED',
                                                                    'CANCELLED',
                                                                    'ACTIVE',
                                                                    'COMPLETED'
    ));
