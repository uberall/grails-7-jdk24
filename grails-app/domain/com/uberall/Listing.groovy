package com.uberall

import org.joda.time.DateTime
import uberall.usertype.dateandtime.OffsetDateTimeUTCType
import uberall.usertype.dateandtime.PersistentDateTimeUTC

import java.time.OffsetDateTime

class Listing {
    static directories = ['Google', 'Facebook', 'Yelp', 'Apple', 'Foursquare']
    static statuses = ['Active', 'InSync', 'OutOfSync', 'Deleted']

    Location location
    String directory
    String status
    DateTime dateCreated
    OffsetDateTime lastUpdated

    static belongsTo = [location:Location]

    static mapping = {
        dateCreated sqlType: 'datetime', type: PersistentDateTimeUTC
        lastUpdated sqlType: 'datetime', type: OffsetDateTimeUTCType
    }

    String toString() {
        "$directory $status $lastUpdated"
    }

    static constraints = {
        status blank: false, nullable: false, inList: statuses
        directory blank: false, nullable: false, inList: directories
        location nullable: false
    }
}
