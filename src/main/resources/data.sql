-- Create admin user (password: admin123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, active)
VALUES ('admin@creche.com', '$2a$10$3YGP.zxnWXP8pFXL.2CeE.QSHJpYyUEPX1p8.E/TUQhvDQZIVtGHK', 'Admin', 'User', 'ROLE_ADMIN', true);

-- Create some test parents
INSERT IGNORE INTO users (email, password, first_name, last_name, role, active)
VALUES 
('parent1@test.com', '$2a$10$3YGP.zxnWXP8pFXL.2CeE.QSHJpYyUEPX1p8.E/TUQhvDQZIVtGHK', 'Parent', 'One', 'ROLE_PARENT', true),
('parent2@test.com', '$2a$10$3YGP.zxnWXP8pFXL.2CeE.QSHJpYyUEPX1p8.E/TUQhvDQZIVtGHK', 'Parent', 'Two', 'ROLE_PARENT', true);

-- Create some test children
INSERT IGNORE INTO children (first_name, last_name, birth_date, parent_id, active, allergies, phone_number, address)
SELECT 'John', 'Doe', '2020-01-15', u.id, true, 'None', '123-456-7890', '123 Main St'
FROM users u WHERE u.email = 'parent1@test.com';

INSERT IGNORE INTO children (first_name, last_name, birth_date, parent_id, active, allergies, phone_number, address)
SELECT 'Jane', 'Doe', '2021-03-20', u.id, true, 'Peanuts', '123-456-7891', '124 Main St'
FROM users u WHERE u.email = 'parent1@test.com'; 