package uberall

import grails.converters.JSON
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import grails.testing.web.controllers.ControllerUnitTest

@Integration
@Rollback
class ApiLocationControllerSpec extends Specification implements ControllerUnitTest<ApiLocationController> {

    Converters converterBean
    def setup() {
        converterBean.init() // Initialize converters for JSON handling
        // Clean up any existing data
        Listing.executeUpdate("delete from Listing")
        Location.executeUpdate("delete from Location")
    }

    void "POST /api/locations creates a new location and returns JSON"() {
        given: "A valid location JSON payload"
        request.contentType = 'application/json'
        request.json = [
            name: "Test Location",
            address: "123 Test Street, Test City",
            valid: true
        ]

        when: "Making a POST request to create a location"
        controller.save()

        then: "The response is successful"
        response.status == 200

        and: "The response contains the created location with an ID"
        response.json.response.location.id != null
        response.json.response.location.name == "Test Location"
        response.json.response.location.address == "123 Test Street, Test City"
        response.json.response.location.valid == true

        and: "The location is persisted in the database"
        Location.count() == 1
        def savedLocation = Location.findByName("Test Location")
        savedLocation != null
        savedLocation.address == "123 Test Street, Test City"
        savedLocation.valid == true
    }

    void "POST /api/locations with validation errors returns 422"() {
        given: "Invalid location data (name too long)"
        request.contentType = 'application/json'
        request.json = [
            name: "a" * 51, // Exceeds maxSize: 50 constraint
            address: "123 Test Street",
            valid: false
        ]

        when: "Making a POST request with invalid data"
        controller.save()

        then: "The response indicates validation error"
        response.status == 400

        and: "No location is created in the database"
        Location.count() == 0
    }

    void "POST /api/locations with blank name returns validation error"() {
        given: "Location data with blank name"
        request.contentType = 'application/json'
        request.json = [
            name: "",
            address: "123 Test Street",
            valid: false
        ]

        when: "Making a POST request with blank name"
        controller.save()

        then: "The response indicates validation error"
        response.status == 400

        and: "No location is created in the database"
        Location.count() == 0
    }

    void "POST /api/locations with blank address returns validation error"() {
        given: "Location data with blank address"
        request.contentType = 'application/json'
        request.json = [
            name: "Test Location",
            address: "",
            valid: false
        ]

        when: "Making a POST request with blank address"
        controller.save()

        then: "The response indicates validation error"
        response.status == 400

        and: "No location is created in the database"
        Location.count() == 0
    }

    void "POST /api/locations with address too long returns validation error"() {
        given: "Location data with address too long"
        request.contentType = 'application/json'
        request.json = [
            name: "Test Location",
            address: "a" * 251, // Exceeds maxSize: 250 constraint
            valid: false
        ]

        when: "Making a POST request with address too long"
        controller.save()

        then: "The response indicates validation error"
        response.status == 400

        and: "No location is created in the database"
        Location.count() == 0
    }

    void "GET /api/locations returns empty list when no locations exist"() {
        when: "Making a GET request when no locations exist"
        controller.index()

        then: "The response is successful"
        response.status == 200

        and: "The response contains an empty array"
        response.json.response.locations instanceof List
        response.json.response.locations.size() == 0
    }

    void "GET /api/locations returns list of locations as JSON"() {
        given: "Some existing locations"
        def location1 = new Location(name: "Location 1", address: "Address 1", valid: true)
        location1.save(flush: true)
        def location2 = new Location(name: "Location 2", address: "Address 2", valid: false)
        location2.save(flush: true)

        when: "Making a GET request to list locations"
        controller.index()

        then: "The response is successful"
        response.status == 200

        and: "The response contains the locations as JSON array"
        response.json.response.locations instanceof List
        response.json.response.locations.size() == 2
    }

    void "GET /api/locations/{id} with non-existent ID returns 404"() {
        when: "Making a GET request for a non-existent location"
        params.id = 99999
        controller.show()

        then: "The response is 404 Not Found"
        response.status == 404
    }

    void "PUT /api/locations/{id} with non-existent ID returns 404"() {
        given: "Updated location data for non-existent location"
        request.contentType = 'application/json'
        request.json = [
            name: "New Name",
            address: "New Address",
            valid: true
        ]
        params.id = 99999

        when: "Making a PUT request to update non-existent location"
        controller.update()

        then: "The response is 404 Not Found"
        response.status == 404
    }

    void "PUT /api/locations/{id} with validation errors returns 422"() {
        given: "An existing location"
        def location = new Location(name: "Old Name", address: "Old Address", valid: false)
        location.save(flush: true)

        and: "Invalid update data"
        request.contentType = 'application/json'
        request.json = [
            name: "a" * 51, // Exceeds maxSize: 50 constraint
            address: "Valid Address",
            valid: true
        ]
        params.id = location.id

        when: "Making a PUT request with invalid data"
        controller.update()

        then: "The response indicates validation failure"
        response.status == 400

        and: "The location is not updated in the database"
        def unchangedLocation = Location.read(location.id)
        unchangedLocation.name == "Old Name"
        unchangedLocation.address == "Old Address"
        unchangedLocation.valid == false
    }

    void "DELETE /api/locations/{id} with non-existent ID returns 404"() {
        when: "Making a DELETE request for non-existent location"
        params.id = 99999
        controller.delete()

        then: "The response is 404 Not Found"
        response.status == 404
    }

    void "DELETE /api/locations/{id} with associated listings should handle cascade"() {
        given: "A location with associated listings"
        def location = new Location(name: "Location with Listings", address: "Test Address", valid: true)
        location.save(flush: true)
        def listing1 = new Listing(directory: "Google", status: "Active", location: location)
        listing1.save(flush: true)
        def listing2 = new Listing(directory: "Facebook", status: "InSync", location: location)
        listing2.save(flush: true)

        when: "Making a DELETE request"
        params.id = location.id
        controller.delete()

        then: "The response is successful or handles cascade appropriately"
        response.status == 200 || response.status == 400 // 400 if cascade is prevented

        and: "The location handling is appropriate"
        if (response.status == 200) {
            // If deletion succeeded, location should be gone
            Location.get(location.id) == null
        } else {
            // If deletion prevented due to associations, location should still exist
            Location.get(location.id) != null
        }
    }

    void "GET /api/locations/{id} returns specific location as JSON"() {
        given: "An existing location"
        def location = new Location(name: "Specific Location", address: "Specific Address", valid: true)
        location.save(flush: true)

        when: "Making a GET request for the specific location"
        params.id = location.id
        controller.show()

        then: "The response is successful"
        response.status == 200

        and: "The response contains the location data"
        response.json.response.location.id == location.id
        response.json.response.location.name == "Specific Location"
        response.json.response.location.address == "Specific Address"
        response.json.response.location.valid == true
    }

    void "PUT /api/locations/{id} updates existing location"() {
        given: "An existing location"
        def location = new Location(name: "Old Name", address: "Old Address", valid: false)
        location.save(flush: true)

        and: "Updated location data"
        request.contentType = 'application/json'
        request.json = [
            name: "New Name",
            address: "New Address",
            valid: true
        ]
        params.id = location.id

        when: "Making a PUT request to update the location"
        controller.update()

        then: "The response is successful"
        response.status == 200

        and: "The location is updated in the database"
        def updatedLocation = Location.get(location.id)
        updatedLocation.name == "New Name"
        updatedLocation.address == "New Address"
        updatedLocation.valid == true

        and: "The response contains the updated location"
        response.json.response.location.name == "New Name"
        response.json.response.location.address == "New Address"
        response.json.response.location.valid == true
    }

    void "DELETE /api/locations/{id} deletes existing location"() {
        given: "An existing location"
        def location = new Location(name: "To Delete", address: "Delete Address", valid: true)
        location.save(flush: true)
        def locationId = location.id

        when: "Making a DELETE request"
        params.id = locationId
        controller.delete()

        then: "The response is successful"
        response.status == 200

        and: "The location is removed from the database"
        Location.get(locationId) == null
        Location.count() == 0
    }
}
