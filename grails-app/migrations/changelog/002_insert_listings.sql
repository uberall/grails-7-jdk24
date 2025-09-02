--liquibase formatted sql
--changeset rmondejar:insert-multiple-listings splitStatements:true endDelimiter:;
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (1, 'Google', 'Active', 1, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (2, 'Facebook', 'InSync', 1, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (3, 'Yelp', 'OutOfSync', 1, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (4, 'Apple', 'Deleted', 1, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (5, 'Foursquare', 'Active', 2, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (6, 'Google', 'InSync', 2, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (7, 'Facebook', 'OutOfSync', 2, 0, NOW(), NOW());
INSERT IGNORE INTO listing (id, directory, status, location_id, version, date_created, last_updated) VALUES (8, 'Yelp', 'Deleted', 2, 0, NOW(), NOW());