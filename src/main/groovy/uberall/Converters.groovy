package uberall

import grails.converters.JSON

class Converters {
    void init() {
        JSON.registerObjectMarshaller(Location) { Location it ->
            def result = [
                id          : it.id,
                dateCreated : it.dateCreated,
                lastUpdated : it.lastUpdated,
                name        : it.name,
                address     : it.address,
                valid       : it.valid
            ]
            return result
        }

        JSON.registerObjectMarshaller(Listing) { Listing it ->
            def result = [
                id          : it.id,
                dateCreated : it.dateCreated,
                lastUpdated : it.lastUpdated,
                directory   : it.directory,
                status      : it.status,
                location    : it.location ? [
                    id      : it.location.id,
                    name    : it.location.name,
                    address : it.location.address
                ] : null
            ]
            return result
        }
    }
}
