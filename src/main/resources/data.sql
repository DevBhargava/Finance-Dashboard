-- =============================================================
-- Seed data for local development / manual testing
-- Run AFTER the schema has been created by Hibernate ddl-auto
-- =============================================================

-- Passwords are BCrypt hashes.
-- admin@finance.com    -> password: admin123
-- analyst@finance.com  -> password: analyst123
-- viewer@finance.com   -> password: viewer123

INSERT IGNORE INTO users (name, email, password, role, status, is_deleted, created_at, updated_at)
VALUES
  ('Admin User',    'admin@finance.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN',   'ACTIVE', false, NOW(), NOW()),
  ('Jane Analyst',  'analyst@finance.com', '$2a$10$puhBDvtX9J9tDBWJj.AJie/cZ.RBpQQPo.YI.t9y1A4./e.7llS2K', 'ANALYST', 'ACTIVE', false, NOW(), NOW()),
  ('Bob Viewer',    'viewer@finance.com',  '$2a$10$NYFZ/8WaQ3Qb6FCs.00jDOKChKnMOFqJG91yOX9b67vr3tBPrqgHi', 'VIEWER',  'ACTIVE', false, NOW(), NOW());

-- Sample transactions tied to analyst (id=2)
INSERT IGNORE INTO transactions (amount, type, category, date, description, notes, is_deleted, created_by, created_at, updated_at)
VALUES
  (5000.00, 'INCOME',  'SALARY',        '2024-03-01', 'March salary',          NULL,                    false, 2, NOW(), NOW()),
  (1200.00, 'EXPENSE', 'RENT',          '2024-03-02', 'Monthly rent',          'Paid via bank transfer', false, 2, NOW(), NOW()),
  ( 320.50, 'EXPENSE', 'FOOD',          '2024-03-05', 'Weekly groceries',      NULL,                    false, 2, NOW(), NOW()),
  ( 850.00, 'INCOME',  'FREELANCE',     '2024-03-10', 'Logo design project',   'Client: Acme Corp',     false, 2, NOW(), NOW()),
  ( 150.00, 'EXPENSE', 'UTILITIES',     '2024-03-12', 'Electricity bill',      NULL,                    false, 2, NOW(), NOW()),
  (  75.00, 'EXPENSE', 'ENTERTAINMENT', '2024-03-15', 'Streaming subscriptions', NULL,                  false, 2, NOW(), NOW()),
  ( 200.00, 'EXPENSE', 'HEALTHCARE',    '2024-03-18', 'Dental checkup',        NULL,                    false, 2, NOW(), NOW()),
  (2000.00, 'INCOME',  'INVESTMENT',    '2024-03-20', 'Dividend payout',       'Q1 dividends',          false, 2, NOW(), NOW()),
  ( 400.00, 'EXPENSE', 'TRAVEL',        '2024-03-22', 'Flight tickets',        'Business trip',         false, 2, NOW(), NOW()),
  ( 100.00, 'EXPENSE', 'EDUCATION',     '2024-03-25', 'Online course',         'Spring Boot course',    false, 2, NOW(), NOW()),

  -- April entries
  (5000.00, 'INCOME',  'SALARY',        '2024-04-01', 'April salary',          NULL,                    false, 2, NOW(), NOW()),
  (1200.00, 'EXPENSE', 'RENT',          '2024-04-02', 'Monthly rent',          NULL,                    false, 2, NOW(), NOW()),
  ( 280.00, 'EXPENSE', 'FOOD',          '2024-04-08', 'Groceries',             NULL,                    false, 2, NOW(), NOW()),
  ( 500.00, 'INCOME',  'FREELANCE',     '2024-04-14', 'Consulting work',       NULL,                    false, 2, NOW(), NOW()),
  ( 130.00, 'EXPENSE', 'UTILITIES',     '2024-04-15', 'Water + internet',      NULL,                    false, 2, NOW(), NOW());
