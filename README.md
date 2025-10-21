# Grails 7 (JDK 24) Demonstration Project

This project is a Grails 7 (7.0.0) application targeting JDK 24. The goal is to provide a minimal reference for how to run the app locally and how to run CodeNarc.

## Application Details
- **Grails Version**: 7.0.0
- **JDK Version**: 24.0.2
- **Spring Boot Version**: 3.5.5
- **Groovy Version**: 4.0.28

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

API base path: /api

- List all Locations
  - Request:
    curl -s -X GET "http://localhost:8090/api/locations" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    Returns an array of all locations

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
      "id": 3,
      "name": "Central Park",
      "address": "5th Ave, New York, NY",
      "valid": false,
      "dateCreated": "2025-10-21T11:02:34+02:00",
      "lastUpdated": "2025-10-21T11:02:34+02:00"
    }

- List all Listings
  - Request:
    curl -s -X GET "http://localhost:8090/api/listings" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    Returns an array of all listings

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
            "location": { "id": 3 }
          }'
  - Example response (201 Created):
    {
      "id": 9,
      "directory": "Google",
      "status": "Active",
      "location": { "id": 3, "name": "Central Park", "address": "5th Ave, New York, NY" },
      "dateCreated": "2025-10-21T11:02:39+02:00",
      "lastUpdated": "2025-10-21T11:02:39+02:00"
    }

- Get a Specific Listing
  - Request:
    curl -s -X GET "http://localhost:8090/api/listings/9" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    Returns the specific listing details

- Update a Listing
  - Request:
    curl -s -X PUT "http://localhost:8090/api/listings/9" \
      -H "Content-Type: application/json" \
      -d '{
            "directory": "Facebook",
            "status": "Active"
          }'
  - Example response (200 OK):
    Returns the updated listing

- Delete a Listing
  - Request:
    curl -s -X DELETE "http://localhost:8090/api/listings/9" \
      -H "Content-Type: application/json"
  - Example response (204 No Content):
    No response body

Notes:
- Endpoints follow Grails RESTful resource mappings defined in UrlMappings.groovy.
- If you send form-encoded data instead of JSON, adjust headers and body accordingly.
- The same CRUD operations are available for both locations and listings
