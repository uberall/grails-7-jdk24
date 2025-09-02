package uberall.test

import org.joda.time.DateTime

/**
 * Class is only being used to have a good possibility of checking if the timezone configuration for the database is correct
 * This will give us a pro-active options to catching possible timezone issues
 */
class JodaTimeTesting {
    Long id
    Long createdAtEpochSeconds
    DateTime jodaDateTime
    static mapping = {
        jodaDateTime type: 'org.jadira.usertype.dateandtime.joda.CustomPersistentDateTime', sqlType: 'datetime'
        version false
    }
}
