
-- Orders table stores each completed order with total price
CREATE TABLE IF NOT EXISTS orders (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  total_price DECIMAL(10, 2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Junction table for many-to-many relationship between orders and books
CREATE TABLE IF NOT EXISTS book_orders (
  id INT NOT NULL AUTO_INCREMENT,
  order_id INT NOT NULL,
  book_id INT NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES book(id)
);

-- View to get order details with book information
-- Joins orders table with book_orders junction table and book table to provide
-- complete order information including book titles, prices, and quantities in a single query.
-- This eliminates the need for multiple queries and simplifies order retrieval in the application layer.
CREATE OR REPLACE VIEW order_detail_vw AS
SELECT 
    orders.id,
    orders.user_id,
    orders.total_price,
    orders.created_at,
    book_orders.book_id,
    book.title,
    book_orders.price,
    book_orders.quantity
FROM orders
LEFT JOIN book_orders ON orders.id = book_orders.order_id
LEFT JOIN book ON book_orders.book_id = book.id;
