DELETE FROM bookmarks;
DELETE FROM book_orders;
DELETE FROM library_cards;
DELETE FROM digitized_book_author;
DELETE FROM digitized_books;
DELETE FROM physical_book_author;
DELETE FROM physical_books;
DELETE FROM users WHERE username NOT IN ('username666', 'rogt', 'vsauce', 'queen');