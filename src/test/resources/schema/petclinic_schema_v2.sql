CREATE TABLE owners (
    id BIGINT PRIMARY KEY,
    first_name VARCHAR(80),
    address VARCHAR(255)
);

CREATE TABLE pets (
    id BIGINT PRIMARY KEY,
    name VARCHAR(120),
    owner_id BIGINT
);