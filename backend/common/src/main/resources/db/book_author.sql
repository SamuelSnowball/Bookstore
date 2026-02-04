CREATE or replace view book_author_vw as 
( 
  select 
  book.id, 
  book.author_id, 
  book.title,
  book.price,
  book.description, 
  author.first_name, 
  author.last_name from book 
  inner join author on book.author_id=author.id
);