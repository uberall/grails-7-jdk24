package com.uberall

import org.joda.time.DateTime
import java.time.OffsetDateTime
import uberall.usertype.dateandtime.OffsetDateTimeUTCType
import uberall.usertype.dateandtime.PersistentDateTimeUTC

class Location {

    String name
    String address
    Boolean valid = false

    OffsetDateTime dateCreated
    DateTime lastUpdated

    static hasMany = [listings: Listing]

    static mapping = {
        dateCreated sqlType: 'datetime', type: OffsetDateTimeUTCType
        lastUpdated sqlType: 'datetime', type: PersistentDateTimeUTC
    }

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
