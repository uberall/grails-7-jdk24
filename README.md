# Grails 7 (JDK 24) Demonstration Project

This project is a Grails 7 (7.0.0) application targeting JDK 24. It demonstrates modern Grails API development patterns with a focus on the **Uberall API Response wrapper format** from Grails 3.3.

## Key Features
- ✅ **Uberall API Pattern**: Controllers extend `AbstractApiController` with `renderJson` and `ResponseUtil`
- ✅ **Response Wrapper Format**: All API responses use standardized Response objects
- ✅ **Full CRUD Operations**: Complete REST API for Locations and Listings
- ✅ **Error Handling**: Proper HTTP status codes and error messages
- ✅ **Comprehensive Tests**: 51 integration tests with 100% pass rate

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

All API responses follow the Uberall API standard response format with wrapped payloads.

- List all Locations
  - Request:
    curl -s -X GET "http://localhost:8090/api/locations" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "locations": [
          {
            "id": 1,
            "name": "Downtown Location",
            "address": "123 Main St",
            "valid": true
          }
        ]
      }
    }

- Create a Location
  - Request:
    curl -s -X POST "http://localhost:8090/api/locations" \
      -H "Content-Type: application/json" \
      -d '{
            "name": "Central Park",
            "address": "5th Ave, New York, NY",
            "valid": true
          }'
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "location": {
          "id": 3,
          "name": "Central Park",
          "address": "5th Ave, New York, NY",
          "valid": true,
          "dateCreated": "2025-10-21T11:02:34+02:00",
          "lastUpdated": "2025-10-21T11:02:34+02:00"
        }
      }
    }

- List all Listings
  - Request:
    curl -s -X GET "http://localhost:8090/api/listings" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "listings": [
          {
            "id": 1,
            "directory": "Google",
            "status": "Active",
            "location": { "id": 1 }
          }
        ]
      }
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
            "location": { "id": 3 }
          }'
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "listing": {
          "id": 9,
          "directory": "Google",
          "status": "Active",
          "location": { 
            "id": 3, 
            "name": "Central Park", 
            "address": "5th Ave, New York, NY" 
          },
          "dateCreated": "2025-10-21T11:02:39+02:00",
          "lastUpdated": "2025-10-21T11:02:39+02:00"
        }
      }
    }

- Get a Specific Listing
  - Request:
    curl -s -X GET "http://localhost:8090/api/listings/9" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "listing": {
          "id": 9,
          "directory": "Google",
          "status": "Active",
          "location": { "id": 3 }
        }
      }
    }

- Update a Listing
  - Request:
    curl -s -X PUT "http://localhost:8090/api/listings/9" \
      -H "Content-Type: application/json" \
      -d '{
            "directory": "Facebook",
            "status": "Active"
          }'
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "listing": {
          "id": 9,
          "directory": "Facebook",
          "status": "Active",
          "location": { "id": 3 }
        }
      }
    }

- Delete a Listing
  - Request:
    curl -s -X DELETE "http://localhost:8090/api/listings/9" \
      -H "Content-Type: application/json"
  - Example response (200 OK):
    {
      "status": "SUCCESS",
      "response": {
        "deleted": true
      }
    }

Notes:
- All API responses use the Uberall Response wrapper format with `status` and `response` fields
- HTTP status codes: 200 (OK), 400 (Bad Request), 404 (Not Found)
- Endpoints follow Grails RESTful resource mappings defined in UrlMappings.groovy
- If you send form-encoded data instead of JSON, adjust headers and body accordingly
- The same CRUD operations are available for both locations and listings

## API Controllers Architecture

### Controllers
- **ApiLocationController**: Extends `AbstractApiController`, handles Location CRUD operations
- **ApiListingController**: Extends `AbstractApiController`, handles Listing CRUD operations

Both controllers use the traditional Uberall API pattern:
- `renderJson(Response)` for rendering responses
- `ResponseUtil` for creating standardized response objects
- `ResponseUtil.getSuccess([key: data])` for successful responses
- `ResponseUtil.getNotFound()` for 404 errors
- `renderErrors()` for validation errors

### Example Controller Implementation
```groovy
class ApiListingController extends AbstractApiController {
    @Transactional(readOnly = true)
    def index() {
        def listings = Listing.list()
        renderJson(ResponseUtil.getSuccess([listings: listings]))
    }

    @Transactional
    def save() {
        def instance = new Listing()
        bindData(instance, request.JSON, [include: allowedFields])
        instance.validate()
        if (instance.hasErrors()) {
            renderErrors(instance)
            return
        }
        instance.save flush:true
        renderJson(ResponseUtil.getSuccess([listing: instance]))
    }
}
```

## Testing

### Run All Tests
```bash
./gradlew test integrationTest --no-daemon
```

### Test Results
- **Unit Tests**: ✅ All passing
- **Integration Tests**: ✅ 51/51 passing (100%)
- **API Endpoints**: ✅ All CRUD operations verified with curl

### Test Coverage
- Location CRUD operations
- Listing CRUD operations  
- Error handling (404, validation errors)
- Response format validation

See `API_TEST_RESULTS.md` for detailed test execution results with curl commands and responses.
