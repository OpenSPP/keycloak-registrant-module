DROP TABLE users;

CREATE TABLE users (
    "id" serial PRIMARY KEY,
    "username" varchar(50) NOT NULL,
    "password" varchar(50) NOT NULL,
    "email" varchar(1024) NOT NULL,
    "emailVerified" boolean,
    "firstName" varchar(255),
    "lastName" varchar(255),
    "picture" varchar(1024),
    "gender" varchar(255),
    "locale" varchar(32),
    "birthDate" date,
    "createdAt" timestamp DEFAULT now(),
    "updatedAt" timestamp
);

INSERT INTO users
    ("id", "username", "password", "email", "emailVerified", "firstName", "lastName", "picture", "gender", "locale", "birthDate")
VALUES
    (1, 'admin', '123456', 'admin@openspp.local', true, 'The', 'Admin', NULL, 'Male', 'en-US', '2022-12-22'),
    (2, 'john', '123456', 'john@openspp.local', true, 'John', 'Doe', NULL, 'Male', 'en-US', '2022-12-22'),
    (3, 'walter', '123456', 'walter@openspp.local', true, 'Walter', 'White', NULL, 'Male', 'en-US', '2022-12-22');
    (4, 'newuser', '123456', 'newuser@openspp.local', false, 'New', 'User', NULL, 'Female', 'en-US', '2022-12-22');


SELECT * FROM users;