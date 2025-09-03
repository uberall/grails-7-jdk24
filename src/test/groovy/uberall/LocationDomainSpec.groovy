package uberall

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class LocationDomainSpec extends Specification implements DomainUnitTest<Location> {

    void "test constraints for name and address and valid"() {
        when:
        def loc = new Location(name: '', address: '', valid: null)

        then:
        !loc.validate()
        loc.errors.getFieldError('name')
        loc.errors.getFieldError('address')
        loc.errors.getFieldError('valid')

        when:
    loc = new Location(name: 'x' * 51, address: 'a', valid: true)

        then:
        !loc.validate()
        loc.errors.getFieldError('name')

        when:
    loc = new Location(name: 'Shop', address: 'a' * 251, valid: false)

        then:
        !loc.validate()
        loc.errors.getFieldError('address')

        when:
        loc = new Location(name: 'Shop', address: 'Street 1', valid: true)

        then:
        loc.validate()
        loc.completeName == 'Shop Street 1'
        loc.toString() == 'Shop Street 1'
    }
}
