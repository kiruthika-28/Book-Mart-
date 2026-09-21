CREATE DATABASE bookmart;

USE bookmart;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(50)
);

CREATE TABLE books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100),
    author VARCHAR(100),
    category VARCHAR(50),
    price DOUBLE,
    stock INT
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    total_amount DOUBLE,
    order_status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT,
    rating INT,
    comment VARCHAR(255),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

INSERT INTO users (username, password)
VALUES
('kiruthika', '1234');

INSERT INTO books
(title, author, category, price, stock)
VALUES
('Java Programming', 'Herbert Schildt', 'Programming', 450, 10),
('Python Basics', 'Mark Lutz', 'Programming', 400, 8),
('Data Science', 'John Smith', 'Data Science', 550, 5),
('Artificial Intelligence', 'Stuart Russell', 'AI', 650, 6);