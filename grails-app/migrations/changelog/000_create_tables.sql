--liquibase formatted sql
--changeset rmondejar:create-location-and-listing-tables splitStatements:true endDelimiter:;
CREATE TABLE IF NOT EXISTS location (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  version BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(1024) NOT NULL,
  valid BOOLEAN NOT NULL,
  date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_location PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS listing (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  version BIGINT NOT NULL,
  directory VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL,
  location_id BIGINT UNSIGNED NOT NULL,
  date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_listing PRIMARY KEY (id),
  CONSTRAINT fk_listing_location FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE INDEX idx_listing_location_id ON listing(location_id);
