--liquibase formatted sql
--changeset rmondejar:alter-ids-auto-increment splitStatements:true endDelimiter:;
-- Ensure primary key IDs are auto-incrementing for MySQL so GORM inserts without specifying ID work.
ALTER TABLE location MODIFY id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE listing MODIFY id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT;
