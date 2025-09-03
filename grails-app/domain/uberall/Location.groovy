package uberall

import org.joda.time.DateTime
import java.time.OffsetDateTime

class Location {

    String name
    String address
    Boolean valid = false

    OffsetDateTime dateCreated
    DateTime lastUpdated

    static hasMany = [listings: Listing]

    static mapping = {
        dateCreated sqlType: 'datetime'
        lastUpdated type: 'org.jadira.usertype.dateandtime.joda.CustomPersistentDateTime', sqlType: 'datetime'
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
