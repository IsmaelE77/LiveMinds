CREATE TABLE role (
  id serial,
   name VARCHAR(255),
   CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE app_user (
  id serial,
   user_name VARCHAR(255) NOT NULL UNIQUE,
   name VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   role_id INTEGER NOT NULL,
   CONSTRAINT pk_appuser PRIMARY KEY (id)
);

ALTER TABLE app_user ADD CONSTRAINT FK_APPUSER_ON_ROLE FOREIGN KEY (role_id) REFERENCES role (id);


CREATE TABLE room (
  id serial,
   name VARCHAR(255) NOT NULL UNIQUE,
   program VARCHAR(255) NOT NULL,
   course VARCHAR(255) NOT NULL,
   professor_class VARCHAR(255) NOT NULL,
   time TIMESTAMP NOT NULL,
   status VARCHAR(255) NOT NULL,
   broadcaster_id INTEGER NOT NULL,
   CONSTRAINT pk_room PRIMARY KEY (id)
);

ALTER TABLE room ADD CONSTRAINT FK_ROOM_ON_BROADCASTER FOREIGN KEY (broadcaster_id) REFERENCES app_user (id);

CREATE TABLE room_banned_users (
    room_id INTEGER,
    user_id INTEGER,
    PRIMARY KEY (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);
