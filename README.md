# Grails 7 (JDK 24) Demonstration Project

This project is a Grails 7 (7.0.0-RC1) application targeting JDK 24. The goal is to provide a minimal reference for how to run the app locally and how to run CodeNarc.

## How to run

1. Start dependencies with Docker Compose (MySQL)
   - cd docker-compose
   - docker-compose up -d

2. Set Java 24 with SDKMAN
   - sdk install java 24.0.2-amzn
   - sdk use java 24.0.2-amzn

3. Run database migrations (Liquibase)
   - Auto-run is disabled by default now.
   - You can run migrations manually via cli:
     - ./gradlew dbmUpdate

4. Run CodeNarc with Gradle:
   - ./gradlew clean codenarcMain codenarcTest codenarcIntegrationTest
   - results : ./build/reports/codenarc
   
This project uses the Gradle CodeNarc plugin with a custom ruleset at:
- src/main/groovy/uberall/UberallRules.groovy

5. Run the application
   - ./gradlew bootRun
   - App will start at http://localhost:8090

## Using the API

API base path: /api/v1

- Create a Location
  - Request:
    curl -s -X POST "http://localhost:8090/api/locations" \
      -H "Content-Type: application/json" \
      -d '{
            "name": "Central Park",
            "address": "5th Ave, New York, NY"
          }'
  - Example response (201 Created):
    {
      "id": 1,
      "name": "Central Park",
      "address": "5th Ave, New York, NY",
      "valid": false
    }

- Create a Listing for a Location
  - Requirements: use an existing location id (e.g., 1 from previous step)
  - Allowed directories: Google, Facebook, Yelp, Apple, Foursquare
  - Allowed statuses: Active, InSync, OutOfSync, Deleted
  - Request:
    curl -s -X POST "http://localhost:8090/api/listings" \
      -H "Content-Type: application/json" \
      -d '{
            "directory": "Google",
            "status": "Active",
            "location": { "id": 1 }
          }'
  - Example response (201 Created):
    {
      "id": 1,
      "directory": "Google",
      "status": "Active",
      "location": { "id": 1 }
    }

Notes:
- Endpoints follow Grails RESTful resource mappings defined in UrlMappings.groovy.
- If you send form-encoded data instead of JSON, adjust headers and body accordingly.
