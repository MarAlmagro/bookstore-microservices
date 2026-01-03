-- Catalog Service Database Initialization Script
-- Creates sample book data for testing and demonstration

USE bookstore_catalog;

-- Create books table if not exists (matches JPA entity structure)
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_isbn (isbn),
    INDEX idx_category (category),
    INDEX idx_author (author)
);

-- Insert 20 sample books with varied genres, authors, and prices
INSERT INTO books (isbn, title, author, description, price, stock, category, created_at, updated_at) VALUES
-- Programming & Technology Books
('9780134685991', 'Effective Java', 'Joshua Bloch', 'Best practices for the Java programming language, covering essential techniques and design patterns for writing robust, efficient, and maintainable code.', 45.99, 100, 'Programming', NOW(), NOW()),
('9780135957059', 'The Pragmatic Programmer', 'Andrew Hunt, David Thomas', 'Your journey to mastery - a guide to becoming a better programmer through practical advice and timeless wisdom.', 49.99, 85, 'Programming', NOW(), NOW()),
('9781617294945', 'Spring Boot in Action', 'Craig Walls', 'Comprehensive guide to building production-ready applications with Spring Boot, covering auto-configuration, testing, and deployment.', 42.50, 75, 'Programming', NOW(), NOW()),
('9780132350884', 'Clean Code', 'Robert C. Martin', 'A handbook of agile software craftsmanship, teaching how to write code that is easy to read, maintain, and extend.', 44.95, 120, 'Programming', NOW(), NOW()),
('9781449373320', 'Designing Data-Intensive Applications', 'Martin Kleppmann', 'The big ideas behind reliable, scalable, and maintainable systems, covering databases, distributed systems, and data processing.', 59.99, 60, 'Technology', NOW(), NOW()),

-- Fiction Books
('9780061120084', 'To Kill a Mockingbird', 'Harper Lee', 'A gripping, heart-wrenching, and wholly remarkable tale of coming-of-age in a South poisoned by virulent prejudice.', 18.99, 200, 'Fiction', NOW(), NOW()),
('9780451524935', '1984', 'George Orwell', 'A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism and mass surveillance.', 16.99, 180, 'Fiction', NOW(), NOW()),
('9780316769174', 'The Catcher in the Rye', 'J.D. Salinger', 'A story about teenage rebellion and alienation that has become a classic of American literature.', 14.99, 150, 'Fiction', NOW(), NOW()),
('9780743273565', 'The Great Gatsby', 'F. Scott Fitzgerald', 'A portrait of the Jazz Age in all its decadence and excess, exploring themes of idealism, resistance to change, and social upheaval.', 15.99, 175, 'Fiction', NOW(), NOW()),

-- Science Fiction & Fantasy
('9780345391803', 'The Hitchhiker''s Guide to the Galaxy', 'Douglas Adams', 'A comedic science fiction series following the misadventures of Arthur Dent as he travels through space.', 17.99, 140, 'Science Fiction', NOW(), NOW()),
('9780547928227', 'The Hobbit', 'J.R.R. Tolkien', 'A fantasy novel about the quest of home-loving hobbit Bilbo Baggins to win a share of treasure guarded by the dragon Smaug.', 19.99, 160, 'Fantasy', NOW(), NOW()),
('9780441013593', 'Dune', 'Frank Herbert', 'An epic science fiction novel set in the distant future amidst a huge interstellar empire, exploring themes of politics, religion, and ecology.', 21.99, 110, 'Science Fiction', NOW(), NOW()),

-- Business & Self-Help
('9781591847816', 'Atomic Habits', 'James Clear', 'An easy and proven way to build good habits and break bad ones through tiny changes that lead to remarkable results.', 27.99, 250, 'Self-Help', NOW(), NOW()),
('9780062316097', 'Sapiens', 'Yuval Noah Harari', 'A brief history of humankind, exploring how Homo sapiens came to dominate the world and what our future might hold.', 24.99, 190, 'Non-Fiction', NOW(), NOW()),
('9781501124020', 'The Lean Startup', 'Eric Ries', 'How today''s entrepreneurs use continuous innovation to create radically successful businesses through validated learning and rapid experimentation.', 29.99, 95, 'Business', NOW(), NOW()),

-- Mystery & Thriller
('9780307949486', 'The Girl with the Dragon Tattoo', 'Stieg Larsson', 'A gripping mystery thriller combining murder investigation, family saga, love story, and financial intrigue.', 19.99, 130, 'Mystery', NOW(), NOW()),
('9780062073488', 'Gone Girl', 'Gillian Flynn', 'A psychological thriller about a marriage gone terribly wrong, featuring unreliable narrators and shocking twists.', 18.99, 145, 'Thriller', NOW(), NOW()),

-- Non-Fiction & Biography
('9780393347777', 'Educated', 'Tara Westover', 'A memoir about a young woman who grows up in a strict and abusive household but eventually escapes to learn about the wider world through education.', 22.99, 165, 'Biography', NOW(), NOW()),
('9780385353755', 'Becoming', 'Michelle Obama', 'An intimate memoir by the former First Lady of the United States, chronicling her experiences from childhood to the White House.', 26.99, 200, 'Biography', NOW(), NOW()),

-- Romance
('9780143127796', 'Me Before You', 'Jojo Moyes', 'A heartbreaking romance about a young woman who becomes a caregiver for a paralyzed man, transforming both their lives.', 16.99, 155, 'Romance', NOW(), NOW());

-- Verify data insertion
SELECT 'Database initialization completed successfully' AS status;
SELECT COUNT(*) AS total_books FROM books;
