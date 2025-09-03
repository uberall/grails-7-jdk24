package uberall

import grails.testing.gorm.DomainUnitTest
import org.joda.time.DateTime
import spock.lang.Specification

class ListingDomainSpec extends Specification implements DomainUnitTest<Listing> {

    void "test constraints and toString"() {
        given:
        def loc = new Location(name: 'Shop', address: 'Street 1', valid: true)

        when: "missing required fields"
        def l = new Listing()

        then:
        !l.validate()
        l.errors.getFieldError('status')
        l.errors.getFieldError('directory')
        l.errors.getFieldError('location')

        when: "invalid inList values"
        l = new Listing(status: 'Wrong', directory: 'Unknown', location: loc)

        then:
        !l.validate()
        l.errors.getFieldError('status')
        l.errors.getFieldError('directory')

        when: "valid values"
        l = new Listing(status: Listing.statuses.first(), directory: Listing.directories.last(), location: loc)
        l.dateCreated = DateTime.now()

        then:
        l.validate()
        l.toString().contains(l.directory)
        l.toString().contains(l.status)
    }
}
