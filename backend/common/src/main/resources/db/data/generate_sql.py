import csv
import os

# Generate INSERT statements for authors (batch 1000 per statement)
print('Generating author INSERT statements...')
with open('authors.csv', 'r', encoding='utf-8') as csv_file, \
     open('insert_authors.sql', 'w', encoding='utf-8') as sql_file:
    
    reader = csv.DictReader(csv_file)
    batch_size = 1000
    batch = []
    
    for row in reader:
        first_name = row['first_name'].replace("'", "''")
        last_name = row['last_name'].replace("'", "''")
        batch.append(f"('{first_name}', '{last_name}')")
        
        if len(batch) >= batch_size:
            sql_file.write(f"INSERT INTO author (first_name, last_name) VALUES\n")
            sql_file.write(',\n'.join(batch))
            sql_file.write(';\n\n')
            batch = []
    
    # Write remaining records
    if batch:
        sql_file.write(f"INSERT INTO author (first_name, last_name) VALUES\n")
        sql_file.write(',\n'.join(batch))
        sql_file.write(';\n\n')

print('Author inserts generated!')

# Generate INSERT statements for books (batch 1000 per statement)
print('Generating book INSERT statements...')
with open('books.csv', 'r', encoding='utf-8') as csv_file, \
     open('insert_books.sql', 'w', encoding='utf-8') as sql_file:
    
    reader = csv.DictReader(csv_file)
    batch_size = 1000
    batch = []
    
    for row in reader:
        author_id = row['author_id']
        title = row['title'].replace("'", "''")
        price = row.get('price', '9.99')  # Default price if not present
        description = row.get('description', 'A captivating read.').replace("'", "''")
        batch.append(f"({author_id}, '{title}', {price}, '{description}')")
        
        if len(batch) >= batch_size:
            sql_file.write(f"INSERT INTO book (author_id, title, price, description) VALUES\n")
            sql_file.write(',\n'.join(batch))
            sql_file.write(';\n\n')
            batch = []
    
    # Write remaining records
    if batch:
        sql_file.write(f"INSERT INTO book (author_id, title, price, description) VALUES\n")
        sql_file.write(',\n'.join(batch))
        sql_file.write(';\n\n')

print('Book inserts generated!')
print('Done! Created insert_authors.sql and insert_books.sql')
