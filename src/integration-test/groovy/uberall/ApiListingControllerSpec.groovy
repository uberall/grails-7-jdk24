package uberall

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Unroll

@Integration
@Rollback
class ApiListingControllerSpec extends Specification implements ControllerUnitTest<ApiListingController> {

    Location testLocation

    def setup() {
        // Clean up any existing data
        Listing.executeUpdate("delete from Listing")
        Location.executeUpdate("delete from Location")

        // Create a test location for listings
        testLocation = new Location(name: "Test Location", address: "Test Address", valid: true)
        testLocation.save(flush: true)
    }

    void "POST /api/listings creates a new listing and returns JSON"() {
        given: "A valid listing JSON payload"
        request.contentType = 'application/json'
        request.json = [
            directory: "Google",
            status: "Active",
            location: [id: testLocation.id]
        ]

        when: "Making a POST request to create a listing"
        controller.save()

        then: "The response is successful"
        response.status == 201

        and: "The response contains the created listing with an ID"
        response.json.id != null
        response.json.directory == "Google"
        response.json.status == "Active"
        response.json.location.id == testLocation.id

        and: "The listing is persisted in the database"
        Listing.count() == 1
        def savedListing = Listing.findByDirectory("Google")
        savedListing != null
        savedListing.status == "Active"
        savedListing.location.id == testLocation.id
    }

    void "POST /api/listings with invalid status returns validation error"() {
        given: "A listing with invalid status"
        request.contentType = 'application/json'
        request.json = [
            directory: "Google",
            status: "InvalidStatus", // Not in allowed statuses
            location: [id: testLocation.id]
        ]

        when: "Making a POST request with invalid status"
        controller.save()

        then: "The response indicates validation failure"
        response.status == 422

        and: "No listing is created in the database"
        Listing.count() == 0
    }

    void "POST /api/listings with non-existent location returns error"() {
        given: "A listing with non-existent location ID"
        request.contentType = 'application/json'
        request.json = [
            directory: "Google",
            status: "Active",
            location: [id: 99999] // Non-existent location ID
        ]

        when: "Making a POST request with invalid location"
        controller.save()

        then: "The response indicates an error"
        response.status == 404

        and: "No listing is created in the database"
        Listing.count() == 0
    }

    void "POST /api/listings with blank directory returns validation error"() {
        given: "A listing with blank directory"
        request.contentType = 'application/json'
        request.json = [
            directory: "",
            status: "Active",
            location: [id: testLocation.id]
        ]

        when: "Making a POST request with blank directory"
        controller.save()

        then: "The response indicates validation failure"
        response.status == 422

        and: "No listing is created in the database"
        Listing.count() == 0
    }

    void "POST /api/listings with blank status returns validation error"() {
        given: "A listing with blank status"
        request.contentType = 'application/json'
        request.json = [
            directory: "Google",
            status: "",
            location: [id: testLocation.id]
        ]

        when: "Making a POST request with blank status"
        controller.save()

        then: "The response indicates validation failure"
        response.status == 422

        and: "No listing is created in the database"
        Listing.count() == 0
    }

    void "GET /api/listings returns empty list when no listings exist"() {
        when: "Making a GET request when no listings exist"
        controller.index()

        then: "The response is successful"
        response.status == 200

        and: "The response contains an empty array"
        response.json instanceof List
        response.json.size() == 0
    }

    void "GET /api/listings returns list of listings as JSON"() {
        given: "Some existing listings"
        def listing1 = new Listing(directory: "Google", status: "Active", location: testLocation)
        listing1.save(flush: true)
        def listing2 = new Listing(directory: "Facebook", status: "InSync", location: testLocation)
        listing2.save(flush: true)

        when: "Making a GET request to list listings"
        controller.index()

        then: "The response is successful"
        response.status == 200

        and: "The response contains the listings as JSON array"
        response.json instanceof List
        response.json.size() == 2
        response.json.find { it.directory == "Google" && it.status == "Active" } != null
        response.json.find { it.directory == "Facebook" && it.status == "InSync" } != null
    }

    void "GET /api/listings/{id} with non-existent ID returns 404"() {
        when: "Making a GET request for a non-existent listing"
        params.id = 99999
        controller.show()

        then: "The response is 404 Not Found"
        response.status == 404
    }

    void "PUT /api/listings/{id} with non-existent ID returns 404"() {
        given: "Updated listing data for non-existent listing"
        request.contentType = 'application/json'
        request.json = [
            directory: "Apple",
            status: "Deleted",
            location: [id: testLocation.id]
        ]
        params.id = 99999

        when: "Making a PUT request to update non-existent listing"
        controller.update()

        then: "The response is 404 Not Found"
        response.status == 404
    }

    void "PUT /api/listings/{id} with validation errors returns 422"() {
        given: "An existing listing"
        def listing = new Listing(directory: "Google", status: "Active", location: testLocation)
        listing.save(flush: true)

        and: "Invalid update data"
        request.contentType = 'application/json'
        request.json = [
            directory: "InvalidDirectory", // Not in allowed directories
            status: "Active",
            location: [id: testLocation.id]
        ]
        params.id = listing.id

        when: "Making a PUT request with invalid data"
        controller.update()

        then: "The response indicates validation failure"
        response.status == 422

        and: "The listing is not updated in the database"
        def unchangedListing = Listing.get(listing.id)
        unchangedListing.directory == "Google"
        unchangedListing.status == "Active"
    }

    void "DELETE /api/listings/{id} with non-existent ID returns 404"() {
        when: "Making a DELETE request for non-existent listing"
        params.id = 99999
        controller.delete()

        then: "The response is 404 Not Found"
        response.status == 404
    }

    void "GET /api/listings/{id} returns specific listing as JSON"() {
        given: "An existing listing"
        def listing = new Listing(directory: "Yelp", status: "OutOfSync", location: testLocation)
        listing.save(flush: true)

        when: "Making a GET request for the specific listing"
        params.id = listing.id
        controller.show()

        then: "The response is successful"
        response.status == 200

        and: "The response contains the listing data"
        response.json.id == listing.id
        response.json.directory == "Yelp"
        response.json.status == "OutOfSync"
        response.json.location.id == testLocation.id
    }

    void "PUT /api/listings/{id} updates existing listing"() {
        given: "An existing listing"
        def listing = new Listing(directory: "Google", status: "Active", location: testLocation)
        listing.save(flush: true)

        and: "Updated listing data"
        request.contentType = 'application/json'
        request.json = [
            directory: "Apple",
            status: "Deleted",
            location: [id: testLocation.id]
        ]
        params.id = listing.id

        when: "Making a PUT request to update the listing"
        controller.update()

        then: "The response is successful"
        response.status == 200

        and: "The listing is updated in the database"
        def updatedListing = Listing.get(listing.id)
        updatedListing.directory == "Apple"
        updatedListing.status == "Deleted"
        updatedListing.location.id == testLocation.id
    }

    void "DELETE /api/listings/{id} deletes existing listing"() {
        given: "An existing listing"
        def listing = new Listing(directory: "Foursquare", status: "Active", location: testLocation)
        listing.save(flush: true)
        def listingId = listing.id

        when: "Making a DELETE request"
        params.id = listingId
        controller.delete()

        then: "The response is successful"
        response.status == 204

        and: "The listing is removed from the database"
        Listing.get(listingId) == null
        Listing.count() == 0
    }

    @Unroll
    void "PUT updates to directory #directory"() {
        given:
        def listing = new Listing(directory: "Google", status: "Active", location: testLocation).save(flush: true)

        when:
        request.contentType = 'application/json'
        request.json = [directory: directory, status: 'Active', location: [id: testLocation.id]]
        params.id = listing.id
        controller.update()

        then:
        response.status == 200
        Listing.get(listing.id).directory == directory

        where:
        directory << Listing.directories
    }

    @Unroll
    void "POST /api/listings creates with valid status #status"() {
        given:
        request.contentType = 'application/json'
        request.json = [
            directory: "Google",
            status: status,
            location: [id: testLocation.id]
        ]

        when:
        controller.save()

        then:
        response.status == 201
        Listing.findByStatus(status) != null

        where:
        status << Listing.statuses
    }

    @Unroll
    void "PUT /api/listings updates directory to #directory"() {
        given:
        def listing = new Listing(directory: "Google", status: "Active", location: testLocation).save(flush: true)

        when:
        request.contentType = 'application/json'
        request.json = [
            directory: directory,
            status: "Active",
            location: [id: testLocation.id]
        ]
        params.id = listing.id
        controller.update()

        then:
        response.status == 200
        Listing.get(listing.id).directory == directory

        where:
        directory << Listing.directories
    }

    @Unroll
    void "PUT /api/listings updates status to #status"() {
        given:
        def listing = new Listing(directory: "Google", status: "Active", location: testLocation).save(flush: true)

        when:
        request.contentType = 'application/json'
        request.json = [
            directory: "Google",
            status: status,
            location: [id: testLocation.id]
        ]
        params.id = listing.id
        controller.update()

        then:
        response.status == 200
        Listing.get(listing.id).status == status

        where:
        status << Listing.statuses
    }
}
