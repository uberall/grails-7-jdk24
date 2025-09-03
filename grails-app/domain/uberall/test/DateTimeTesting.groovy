package uberall.test

import org.joda.time.DateTime

import java.time.OffsetDateTime

/**
 * Class is only being used to have a good possibility of checking if the timezone configuration for the database is correct
 * This will give us a pro-active options to catching possible timezone issues
 */
class DateTimeTesting {
    Long id
    Long createdAtEpochSeconds
    DateTime jodaDateTime
    OffsetDateTime javaDateTime
    static mapping = {
        jodaDateTime type: 'org.jadira.usertype.dateandtime.joda.CustomPersistentDateTime', sqlType: 'datetime'
        javaDateTime sqlType: 'datetime'
        version false
    }
}
