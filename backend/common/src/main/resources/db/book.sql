CREATE TABLE if not exists book (
  id int NOT NULL AUTO_INCREMENT,
  author_id int NOT NULL,
  title varchar(255) DEFAULT NULL,
  price DECIMAL(10, 2) DEFAULT 0.00,
  description TEXT,
  PRIMARY KEY (id),
  FOREIGN KEY (author_id) references author(id)
);