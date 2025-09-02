package uberall.test

import java.time.OffsetDateTime

/**
 * Class is only being used to have a good possibility of checking if the timezone configuration for the database is correct
 * This will give us a pro-active options to catching possible timezone issues
 */
class JavaTimeTesting {
    Long id
    Long createdAtEpochSeconds
    OffsetDateTime javaDateTime
    static mapping = {
        javaDateTime sqlType: 'datetime'
        version false
    }
}
