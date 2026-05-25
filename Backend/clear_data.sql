-- ============================================================
-- Zuply — Clear all data (keep tables & schema intact)
-- Run in MySQL Workbench or CLI:
--   mysql -u root -p zuply_db < clear_data.sql
-- ============================================================

USE zuply_db;

-- Disable FK checks so we can truncate in any order
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE reviews;
TRUNCATE TABLE tags;
TRUNCATE TABLE payments;
TRUNCATE TABLE wishlists;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE carts;
TRUNCATE TABLE order_items;
TRUNCATE TABLE customer_orders;
TRUNCATE TABLE listing_products;
TRUNCATE TABLE images;
TRUNCATE TABLE products;
TRUNCATE TABLE sellers;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;

-- Re-enable FK checks
SET FOREIGN_KEY_CHECKS = 1;

-- Confirm
SELECT 'All data cleared successfully. Tables and schema are intact.' AS status;
