create table prods (
    id uuid not null primary key,
    sku text not null unique,
    price float not null check ( price > 0.0 )
);
