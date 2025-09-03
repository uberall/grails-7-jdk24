package uberall

import groovy.transform.CompileStatic

@CompileStatic
class LocationService {

    LocationDataService locationDataService

    Location save(Location location) throws IllegalArgumentException {
        location.valid = isValidAddress(location.address)
        locationDataService.save(location)
    }

    private static Boolean isValidAddress(String address) {
        // Dummy validation logic for example purposes
        return address?.trim()?.length() > 10
    }
}
