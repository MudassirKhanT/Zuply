-- ============================================================
-- Zuply — Create Admin User
-- Password: Admin@123
-- Run in MySQL Workbench or CLI:
--   mysql -u root -p zuply_db < create_admin.sql
-- ============================================================

USE zuply_db;

INSERT INTO users (name, email, password, phone, role)
VALUES (
  'Admin',
  'admin@zuply.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkMxHBUijWm',
  '9999999999',
  'ADMIN'
);

SELECT 'Admin user created successfully.' AS status;
SELECT id, name, email, role FROM users WHERE email = 'admin@zuply.com';
