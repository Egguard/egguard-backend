CREATE USER egguard_user WITH PASSWORD 'password';
CREATE DATABASE egguard_db OWNER egguard_user;
GRANT ALL PRIVILEGES ON DATABASE egguard_db TO egguard_user;
