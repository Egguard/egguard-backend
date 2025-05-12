-- Roles
INSERT INTO roles (id, role) VALUES 
(1, 'ADMIN'),
(2, 'USER');

-- Users (passwords would be hashed in a real application)
INSERT INTO users (id, email, name, surnames, photo_url, phone, country, city, zip, address, role_id) VALUES
(1, 'admin@egguard.com', 'Admin', 'User', 'https://example.com/admin.jpg', '+1234567890', 'Spain', 'Madrid', '28001', 'Admin Street 1', 1),
(2, 'user@egguard.com', 'Normal', 'User', 'https://example.com/user.jpg', '+0987654321', 'Spain', 'Barcelona', '08001', 'User Avenue 2', 2);

-- Farms
INSERT INTO farms (id, name, photo_url, latitude, longitude, user_id) VALUES
(1, 'North Farm', 'https://example.com/farm1.jpg', 40.4168, -3.7038, 1),
(2, 'South Farm', 'https://example.com/farm2.jpg', 40.4000, -3.6833, 1),
(3, 'East Farm', 'https://example.com/farm3.jpg', 41.3851, 2.1734, 2),
(4, 'West Farm', 'https://example.com/farm4.jpg', 41.3700, 2.1500, 2);

-- Robots
INSERT INTO robots (id, purchase_date, farm_id, status) VALUES
(1, '2023-01-15 10:00:00', 1, 'ACTIVE'),
(2, '2023-02-20 11:30:00', 1, 'INACTIVE'),
(3, '2023-03-10 09:15:00', 2, 'ACTIVE'),
(4, '2023-04-05 14:45:00', 2, 'MAINTENANCE'),
(5, '2023-05-12 08:30:00', 3, 'ACTIVE'),
(6, '2023-06-18 16:20:00', 3, 'ERROR'),
(7, '2023-07-22 13:10:00', 4, 'ACTIVE'),
(8, '2023-08-30 10:50:00', 4, 'INACTIVE');

-- Notifications for each farm
DO $$
DECLARE
    farm_id INT;
    severity_types TEXT[] := ARRAY['INFO', 'WARNING', 'CRITICAL'];
    severity_type TEXT;
    i INT;
BEGIN
    FOR farm_id IN 1..4 LOOP
        FOR i IN 1..20 LOOP
            -- Select random severity
            severity_type := severity_types[1 + floor(random() * 3)];
            
            INSERT INTO notifications (farm_id, timestamp, severity, message, photo_url)
            VALUES (
                farm_id,
                NOW() - (random() * interval '30 days'),
                severity_type,
                CASE severity_type
                    WHEN 'INFO' THEN 'La bateria de tu robot se ha cargado al máximo'
                    WHEN 'WARNING' THEN 'Se ha detectado un depredador en tu granja'
                    ELSE 'Avería detectada en tu robot, contáctanos'
                END,
                CASE 
                    WHEN random() > 0.5 THEN 'https://picsum.photos/200'
                    ELSE NULL
                END
            );
        END LOOP;
    END LOOP;
END $$;

-- Eggs for each farm
DO $$
DECLARE
    farm_id INT;
    i INT;
BEGIN
    FOR farm_id IN 1..4 LOOP
        FOR i IN 1..20 LOOP
            INSERT INTO eggs (farm_id, coord_x, coord_y, picked, broken, timestamp)
            VALUES (
                farm_id,
                random() * 100,
                random() * 100,
                random() > 0.7,  -- 30% chance of being picked
                random() > 0.8,  -- 20% chance of being broken
                NOW() - (random() * interval '14 days')
            );
        END LOOP;
    END LOOP;
END $$;
