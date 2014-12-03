
-- Table: room
CREATE TABLE room ( 
    name     VARCHAR PRIMARY KEY
                     NOT NULL
                     UNIQUE,
    password VARCHAR NOT NULL 
);


-- Table: room_user
CREATE TABLE room_user ( 
    username VARCHAR NOT NULL
                     UNIQUE
                     REFERENCES users ( username ),
    name     VARCHAR NOT NULL
                     UNIQUE
                     REFERENCES room ( name ) 
);


-- Table: user
CREATE TABLE user ( 
    username VARCHAR PRIMARY KEY
                     NOT NULL
                     UNIQUE,
    password VARCHAR NOT NULL 
);

INSERT INTO [user] ([username], [password]) VALUES ('test', 'test');
