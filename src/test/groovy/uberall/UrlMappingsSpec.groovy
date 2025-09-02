package uberall

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification
import uberall.admin.AdminListingController
import uberall.admin.AdminLocationController

class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    Class[] getControllersToMock() {
        [AdminListingController, AdminLocationController] as Class[]
    }

    void "test admin listing mapping"() {
        expect:
        assertForwardUrlMapping("/admin/listing/show/1", controller: 'adminListing', action: 'show', id: '1')
        assertForwardUrlMapping("/admin/listing/edit/42", controller: 'adminListing', action: 'edit', id: '42')
        assertForwardUrlMapping("/admin/listing/create", controller: 'adminListing', action: 'create')
    }

    void "test admin location mapping"() {
        expect:
        assertForwardUrlMapping("/admin/location/show/1", controller: 'adminLocation', action: 'show', id: '1')
        assertForwardUrlMapping("/admin/location/edit/42", controller: 'adminLocation', action: 'edit', id: '42')
        assertForwardUrlMapping("/admin/location/create", controller: 'adminLocation', action: 'create')
    }

    void "test default mapping to index"() {
        expect:
        assertForwardUrlMapping("/", view: '/index')
    }
}
