-- tables
DROP TABLE if exists CONTINENT;
CREATE TABLE CONTINENT(
    id         varchar(32)      not null    primary key,
    name       varchar(256)     not null,
    version    int              default 0,
    created    timestamp        default current_timestamp,
    modified    timestamp        default current_timestamp
);


DROP TABLE if exists COUNTRY;
CREATE TABLE COUNTRY(
    id         varchar(32)      not null    primary key,
    name       varchar(256)     not null,
    continent  varchar(32)      not null,
    version    int              default 0,
    created    timestamp        default current_timestamp,
    modified    timestamp        default current_timestamp
);


DROP TABLE if exists CITY;
CREATE TABLE CITY(
    id              varchar(32)     not null    primary key,
    name            varchar(256)    not null,
    population      int             default 0,
    country         varchar(32)     not null,
    version         int             default 0,
    created         timestamp       default current_timestamp,
    modified         timestamp       default current_timestamp
);
