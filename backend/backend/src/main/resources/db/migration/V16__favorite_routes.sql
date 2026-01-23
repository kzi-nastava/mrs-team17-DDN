create table if not exists favorite_routes (
                                               id bigserial primary key,
                                               user_id bigint not null,
                                               name varchar(100),

    start_address varchar(255) not null,
    start_lat double precision not null,
    start_lng double precision not null,

    destination_address varchar(255) not null,
    dest_lat double precision not null,
    dest_lng double precision not null,

    created_at timestamptz not null default now(),

    constraint fk_favorite_routes_user
    foreign key (user_id) references users(id)
    on delete cascade
    );

create index if not exists idx_favorite_routes_user_id
    on favorite_routes(user_id);

create unique index if not exists ux_favorite_routes_user_route
    on favorite_routes(
    user_id,
    start_address, start_lat, start_lng,
    destination_address, dest_lat, dest_lng
    );


create table if not exists favorite_route_stops (
                                                    id bigserial primary key,

                                                    favorite_route_id bigint not null,
                                                    stop_order int not null,

                                                    address varchar(255) not null,
    lat double precision not null,
    lng double precision not null,

    constraint fk_favorite_route_stops_route
    foreign key (favorite_route_id) references favorite_routes(id)
    on delete cascade,

    constraint ck_favorite_route_stop_order_positive
    check (stop_order >= 1),

    constraint ux_favorite_route_stop_order
    unique (favorite_route_id, stop_order)
    );

create index if not exists idx_favorite_route_stops_route_id
    on favorite_route_stops(favorite_route_id);
