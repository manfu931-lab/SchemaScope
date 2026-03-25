CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(64),
    name VARCHAR(100)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    status VARCHAR(16),
    user_id BIGINT
);