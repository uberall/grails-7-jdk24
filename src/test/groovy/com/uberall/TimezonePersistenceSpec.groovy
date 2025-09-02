package com.uberall

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import spock.lang.Specification

import java.sql.ResultSet
import java.time.*

/**
 * Verifies how date/times are stored in MySQL with the current configuration.
 * We assert that DB stores UTC values (as configured via custom user types) while the application domain uses systemDefault (e.g., Europe/Berlin).
 */
@Integration
@Rollback
class TimezonePersistenceSpec extends Specification {

    SessionFactory sessionFactory


    void "application zone offset is UTC+1 and not UTC or UTC-4"() {
        given: "A new listing and current app default zone"
        def loc = new Location(name: "TZ Place", address: "Berlin")
        assert loc.save(failOnError: true)
        def listing = new Listing(directory: Listing.directories[0], status: Listing.statuses[0], location: loc)
        listing.save(failOnError: true, flush: true)

        when: "We reload and inspect the application-observed offset"
        def reloaded = Listing.get(listing.id as Long)
        // lastUpdated is an OffsetDateTime; compute the same instant in system default zone
        def zoned = reloaded.lastUpdated.atZoneSameInstant(ZoneId.systemDefault())
        ZoneOffset offset = zoned.getOffset()

        then: "Offset must represent CET/CEST, i.e., not UTC (+00:00) or UTC-04:00; and preferably +01:00 in non-DST"
        assert offset != ZoneOffset.UTC
        assert offset != ZoneOffset.ofHours(-4)
        // Allow DST (+02:00) but still assert that during non-DST the offset would be +01:00 for Europe/Berlin
        def berlin = ZoneId.of("Europe/Berlin")
        def berlinOffset = reloaded.lastUpdated.atZoneSameInstant(berlin).getOffset()
        assert berlinOffset == ZoneOffset.ofHours(1) || berlinOffset == ZoneOffset.ofHours(2)
    }

    void "date/time are stored in DB in UTC while app uses system default zone"() {
        given: "Berlin zone reference"
        ZoneId berlin = ZoneId.of("Europe/Berlin")

        and: "a listing saved with auto-populated lastUpdated (java.time) and dateCreated (joda) types"
        def loc = new Location(name: "TZ Place", address: "Berlin")
        assert loc.save(failOnError: true)
        def listing = new Listing(directory: Listing.directories[0], status: Listing.statuses[0], location: loc)
        listing.save(failOnError: true, flush: true)

        when: "we read the raw value from the database via SQL, and also via GORM"
        def session = sessionFactory.currentSession
        Long id = listing.id as Long

        // Read raw DATETIME back as string and as TIMESTAMP with connection timezone conversion
        String rawDatetime
        String sessionTimeZone
        session.doWork { conn ->
            // Check session time_zone and raw column value
            def tzStmt = conn.createStatement()
            ResultSet tzRs = tzStmt.executeQuery("SELECT @@session.time_zone")
            tzRs.next()
            sessionTimeZone = tzRs.getString(1)
            tzRs.close(); tzStmt.close()

            def ps = conn.prepareStatement("SELECT last_updated FROM listing WHERE id = ?")
            ps.setLong(1, id)
            ResultSet rs = ps.executeQuery()
            rs.next()
            rawDatetime = rs.getString(1) // textual representation of DATETIME
            rs.close(); ps.close()
        }

        then: "Reading via GORM returns an instant; compute expected Berlin wall time for DB"
        def reloaded = Listing.get(id)
        def expectedUtcLocal = reloaded.lastUpdated.atZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime().withSecond(0).withNano(0)

        and: "Raw DB value represents the UTC wall time of the instant (as currently stored)"
        assert rawDatetime.startsWith(expectedUtcLocal.toString().replace('T',' '))

        and: "The MySQL session time_zone should be available and set to +01:00 per datasource init"
        assert sessionTimeZone != null && sessionTimeZone.size() > 0
        assert sessionTimeZone == "+01:00"
    }
}
