
-- Each row represents an item in a user's cart, which can have a quantity > 1
CREATE TABLE if not exists cart_item (
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  book_id int NOT NULL,
  book_quantity int DEFAULT 1,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) references user(id),
  FOREIGN KEY (book_id) references book(id),
  UNIQUE KEY unique_user_book (user_id, book_id) -- Users cannot have duplicate book entries in their cart
);  

-- Also includes author information
CREATE or replace VIEW cart_item_detail_vw as (
SELECT 
  cart_item.id as cart_item_id,
  cart_item.user_id, 
  cart_item.book_id, 
  cart_item.book_quantity, 
  book_author_vw.author_id, 
  book_author_vw.title,
  book_author_vw.price,
  book_author_vw.description,
  book_author_vw.first_name,
  book_author_vw.last_name
FROM cart_item
INNER JOIN book_author_vw ON cart_item.book_id = book_author_vw.id
);