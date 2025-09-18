package uberall.admin

import org.joda.time.DateTime
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import uberall.Listing
import uberall.ListingDataService
import uberall.Location

import static org.springframework.http.HttpStatus.*

class AdminListingControllerSpec extends Specification implements ControllerUnitTest<AdminListingController>, DataTest {

    @Override
    Class[] getDomainClassesToMock() {
        [Location, Listing]
    }

    Location location

    void setup() {
        controller.listingDataService = Mock(ListingDataService)
        location = new Location(id: 1, name: 'McDonalds', address: 'AlexanderPlatz 1, 10178 Berlin Germany', valid: true)
        location.save(flush:true)
    }

    def populateValidParams(params) {
        assert params != null

        params.status = Listing.statuses[0]
        params.directory = Listing.directories[3]
        params.dateCreated = DateTime.now()
        params.location = location
        params.location.id = 1
        params.controller = 'listing'
    }

    void "Test the index action returns the correct model"() {
        when: "The index action is executed"
        controller.index()

        then: "The model is correct"
        !model.listingList
        model.listingCount == 0
    }

    void "Test the create action returns the correct model"() {
        when: "The create action is executed"
        controller.create()

        then: "The model is correctly created"
        model.listing != null
    }

    void "Test the save action correctly persists an instance"() {
        when: "The save action is executed with an invalid instance"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        Listing listing = new Listing()
        listing.validate()
        controller.save(listing)

        then: "The create view is rendered again with the correct model"
        model.listing != null
        view == 'create'

        when: "The save action is executed with a valid instance"
        response.reset()
        populateValidParams(params)
        listing = new Listing()
        listing.status = params.status
        listing.directory = params.directory
        listing.dateCreated = params.dateCreated
        listing.location = params.location
        listing.validate()
        controller.listingDataService.save(_ as Listing) >> { Listing l -> l.id = 1L; return l }
        controller.save(listing)

        then: "A redirect is issued to the show action"
        response.redirectedUrl == '/admin/listing/show/1'
        controller.flash.message != null
    }

    void "Test that the show action returns the correct model"() {
        when: "The show action is executed with a null domain"
        controller.show(null)

        then: "A 302 error is returned"
        response.status == NOT_FOUND.value()

        when: "A domain instance is passed to the show action"
        populateValidParams(params)
        Listing listing = new Listing()
        listing.status = params.status
        listing.directory = params.directory
        listing.dateCreated = params.dateCreated
        listing.location = params.location
        controller.show(listing)

        then: "A model is populated containing the domain instance"
        model.listing == listing
    }

    void "Test that the edit action returns the correct model"() {
        when: "The edit action is executed with a null domain"
        controller.edit(null)

        then: "A 404 error is returned"
        response.status == NOT_FOUND.value()

        when: "A domain instance is passed to the edit action"
        populateValidParams(params)
        Listing listing = new Listing()
        listing.status = params.status
        listing.directory = params.directory
        listing.dateCreated = params.dateCreated
        listing.location = params.location
        controller.edit(listing)

        then: "A model is populated containing the domain instance"
        model.listing == listing
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when: "Update is called for a domain instance that doesn't exist"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'PUT'
        controller.update(null)

        then:"A 302 error is returned"
        response.redirectedUrl == '/admin/listing/index'
        flash.message != null

        when: "An invalid domain instance is passed to the update action"
        response.reset()
        Listing listing = new Listing()
        listing.validate()
        controller.update(listing)

        then: "The edit view is rendered again with the invalid instance"
        view == 'edit'
        model.listing == listing

        when: "A valid domain instance is passed to the update action"
        response.reset()
        populateValidParams(params)
        listing = new Listing()
        listing.status = params.status
        listing.directory = params.directory
        listing.dateCreated = params.dateCreated
        listing.location = params.location
        listing.save(flush: true)
        controller.update(listing)

        then: "A redirect is issued to the show action"
        listing != null
        response.redirectedUrl == "/admin/listing/show/$listing.id"
        flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when: "The delete action is called for a null instance"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'DELETE'
        controller.delete(null)

        then: "A 302 is returned"
        response.redirectedUrl == '/admin/listing/index'
        flash.message != null

        when: "A domain instance is created"
        response.reset()
        populateValidParams(params)
        Listing listing = new Listing()
        listing.status = params.status
        listing.directory = params.directory
        listing.dateCreated = params.dateCreated
        listing.location = params.location
        listing.save(flush: true)

        then: "It exists"
        Listing.count() == 1

        when: "The domain instance is passed to the delete action"
        controller.delete(listing)

        then: "The instance is deleted"
        Listing.count() == 0
        response.redirectedUrl == '/admin/listing/index'
        flash.message != null
    }
}
