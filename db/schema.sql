create table prods (
    id uuid not null primary key,
    sku text not null,
    price float not null check ( price > 0.0 ),
    title text not null,
    description text not null,
    img_url text not null,
    available boolean not null
);

create table prod_options (
    id uuid not null primary key,
    prod_id uuid not null,
    title text not null,
    price float not null check ( price > 0.0 ),
    available boolean not null,
    foreign key (prod_id) references prods(id) on delete cascade on update cascade
)
