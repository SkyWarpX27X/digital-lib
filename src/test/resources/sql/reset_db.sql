DELETE FROM physical_books;
DELETE FROM digitized_books;
DELETE FROM users WHERE username NOT IN ('username666', 'rogt', 'vsauce', 'queen');
DELETE FROM library_cards;
DELETE FROM book_orders;
DELETE FROM bookmarks;