package uberall

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification

class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    Class[] getControllersToMock() {
        [ListingController, LocationController] as Class[]
    }

    void "test api v1 listings resource mapping"() {
        expect:
        assertForwardUrlMapping("/api/v1/listings", controller: 'listing', action: 'index')
    }

    void "test api v1 locations resource mapping"() {
        expect:
        assertForwardUrlMapping("/api/v1/locations", controller: 'location', action: 'index')
    }

    void "test default mapping to index"() {
        expect:
        assertForwardUrlMapping("/", view: '/index')
    }
}
