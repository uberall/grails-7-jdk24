package com.uberall

class Location {

    String name
    String address
    Boolean valid = false

    static hasMany = [listings: Listing]

    String getCompleteName() {
        "$name $address"
    }

    String toString() {
        completeName
    }

    static constraints = {
        name maxSize: 50, blank: false, nullable: false
        address maxSize: 250, blank: false, nullable: false
        valid nullable: false
    }
}
