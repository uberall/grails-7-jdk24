--liquibase formatted sql
--changeset rmondejar:insert-multiple-locations splitStatements:true endDelimiter:;
INSERT IGNORE INTO location (id, name, address, valid, version) VALUES (1, 'McDonalds', 'AlexanderPlatz 1, 10178 Berlin Germany', true, 0);
INSERT IGNORE INTO location (id, name, address, valid, version) VALUES (2, 'Starbucks', 'AlexanderPlatz 2, 10178 Berlin Germany', true, 0);
