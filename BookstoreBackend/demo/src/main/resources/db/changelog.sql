-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE if not exists author (
  id int NOT NULL AUTO_INCREMENT,
  first_name varchar(255) DEFAULT NULL,
  last_name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE if not exists book (
  id int NOT NULL AUTO_INCREMENT,
  author_id int NOT NULL,
  title varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (author_id) references author(id)
);

-- make a join table?


INSERT IGNORE INTO author (first_name, last_name)    values ('Mark', 'Twain');
INSERT IGNORE INTO book (author_id, title)           values (1, 'The Adventures of Huckleberry Finn');
INSERT IGNORE INTO book (author_id, title)           values (1, 'The Adventures of Tom Sawyer');
 
INSERT IGNORE INTO author (first_name, last_name)    values('Jane', 'Austen');
INSERT IGNORE INTO book (author_id, title)           values (2, 'Pride and Prejudice');

