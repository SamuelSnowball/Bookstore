-- liquibase formatted sql
-- changeset liquibase:4
CREATE TABLE
  if not exists user (
    id int NOT NULL AUTO_INCREMENT,
    username varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL  DEFAULT '$2a$12$jd0nX4mZ4kChbc3jwaFCD.DVcvopRU/Bct.jJgh94kkHyztrgMfHS',
    PRIMARY KEY (id)
  );

-- Insert demo user
INSERT INTO
  user (username, password)
VALUES
  (
    'username',
    '$2a$12$jd0nX4mZ4kChbc3jwaFCD.DVcvopRU/Bct.jJgh94kkHyztrgMfHS'
  ) ON DUPLICATE KEY
UPDATE username = username;