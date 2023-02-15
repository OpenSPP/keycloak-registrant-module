DROP TABLE users;

CREATE TABLE users (
    "id" serial PRIMARY KEY,
    "username" varchar(50) NOT NULL,
    "password" varchar(50) NOT NULL,
    "email" varchar(1024) NOT NULL,
    "firstName" varchar(255),
    "lastName" varchar(255),
    "createdAt" timestamp DEFAULT now(),
    "updatedAt" timestamp
);

INSERT INTO users
    ("id", "username", "password", "email", "firstName", "lastName")
VALUES
    (1, 'admin', '123456', 'admin@openspp.local', 'The', 'Admin'),
    (2, 'john', '123456', 'john@openspp.local', 'John', 'Doe'),
    (3, 'walter', '123456', 'walter@openspp.local', '', 'Walter White');


SELECT * FROM users;