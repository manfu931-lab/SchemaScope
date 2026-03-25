CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(128),
    name VARCHAR(100)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT
);