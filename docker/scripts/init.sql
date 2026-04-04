-- init.sql

-- Datenbank erstellen
CREATE
DATABASE customers;

-- CREATE DATABASE orders;
-- CREATE DATABASE products;
-- CREATE DATABASE notifications;

-- Rechte für den mindgarden-User setzen
GRANT ALL PRIVILEGES ON DATABASE
customers TO mindgarden;

-- GRANT ALL PRIVILEGES ON DATABASE
-- orders TO mindgarden;
-- GRANT ALL PRIVILEGES ON DATABASE
-- payments TO mindgarden;
-- GRANT ALL PRIVILEGES ON DATABASE
-- invoices TO mindgarden;
