package uberall.admin

import spock.lang.Specification
import grails.testing.web.controllers.ControllerUnitTest
import grails.testing.gorm.DataTest
import uberall.Location
import uberall.LocationService

import static org.springframework.http.HttpStatus.*

class AdminLocationControllerSpec extends Specification implements ControllerUnitTest<AdminLocationController>, DataTest {

    @Override
    Class[] getDomainClassesToMock() {
        [Location]
    }

    def setup() {
        controller.locationService = Mock(LocationService)
    }

    def populateValidParams(params) {
        assert params != null
        params.name = 'McDonalds'
        params.address = 'AlexanderPlatz 1, 10178 Berlin Germany'
        params._valid = ''
        params.controller = 'location'
    }

    void "Test the index action returns the correct model"() {
        when: "The index action is executed"
        controller.index()

        then: "The model is correct"
        model.locationList == null
        model.locationCount == 0
    }

    void "Test the create action returns the correct model"() {
        when: "The create action is executed"
        controller.create()

        then: "The model is correctly created"
        model.location != null
    }

    void "Test the save action correctly persists an instance"() {
        when: "The save action is executed with an invalid instance"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        Location location = new Location()
        location.validate()
        controller.save(location)

        then: "The create view is rendered again with the correct model"
        model.location != null
        view == 'create'

        when: "The save action is executed with a valid instance"
        response.reset()
        populateValidParams(params)
        location = new Location(name: params.name, address: params.address, valid: true)
        location.validate()
        controller.locationService.save(_ as Location) >> { Location l -> l.id = 1L; return l }
        controller.save(location)

        then: "A redirect is issued to the show action"
        response.redirectedUrl == '/admin/location/show/1'
        controller.flash.message != null
    }

    void "Test that the show action returns the correct model"() {
        when: "The show action is executed with a null domain"
        controller.show(null)

        then: "A 404 error is returned"
        response.status == NOT_FOUND.value()

        when: "A domain instance is passed to the show action"
        populateValidParams(params)
        Location location = new Location()
        location = new Location()
        location.name = params.name
        location.address = params.address
        controller.show(location)

        then: "A model is populated containing the domain instance"
        model.location == location
    }

    void "Test that the edit action returns the correct model"() {
        when: "The edit action is executed with a null domain"
        controller.edit(null)

        then: "A 404 error is returned"
        response.status == NOT_FOUND.value()

        when: "A domain instance is passed to the edit action"
        populateValidParams(params)
        def location = new Location()
        location = new Location()
        location.name = params.name
        location.address = params.address
        controller.edit(location)

        then: "A model is populated containing the domain instance"
        model.location == location
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when: "Update is called for a domain instance that doesn't exist"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'PUT'
        controller.update(null)

        then:"A 302 error is returned"
        response.redirectedUrl == '/admin/location/index'
        flash.message != null

        when: "An invalid domain instance is passed to the update action"
        response.reset()
        def location = new Location()
        location.validate()
        controller.update(location)

        then: "The edit view is rendered again with the invalid instance"
        view == 'edit'
        model.location == location

        when: "A valid domain instance is passed to the update action"
        response.reset()
        populateValidParams(params)
        location = new Location()
        location = new Location()
        location.name = params.name
        location.address = params.address
        location.save(flush: true)
        controller.update(location)

        then: "A redirect is issued to the show action"
        location != null
        response.redirectedUrl == "/admin/location/show/$location.id"
        flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when: "The delete action is called for a null instance"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'DELETE'
        controller.delete(null)

        then: "A 302 is returned"
        response.redirectedUrl == '/admin/location/index'
        flash.message != null

        when: "A domain instance is created"
        response.reset()
        populateValidParams(params)
        Location location = new Location()
        location = new Location()
        location.name = params.name
        location.address = params.address
        location.save(flush: true)

        then: "It exists"
        Location.count() == 1

        when: "The domain instance is passed to the delete action"
        controller.delete(location)

        then: "The instance is deleted"
        Location.count() == 0
        response.redirectedUrl == '/admin/location/index'
        flash.message != null
    }
}
