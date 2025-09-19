package uberall.test

import grails.testing.mixin.integration.Integration
import org.springframework.transaction.annotation.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Integration
@Transactional
class DateTimeTestingIntegrationSpec extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeTestingIntegrationSpec)
    DataSource dataSource
    TimeZone defaultTz

    def setup() {
        defaultTz = TimeZone.default
        TimeZone.setDefault(TimeZone.getTimeZone('Europe/Berlin'))
    }

    def cleanup() {
        TimeZone.setDefault(defaultTz)
    }

    void "raw SQL storage honors local timezone"() {
        given:
        DateTime jodaDateTime = new DateTime(2024, 1, 1, 10, 10, DateTimeZone.forID('+01:00'))
        OffsetDateTime javaDateTime = OffsetDateTime.of(2024, 1, 1, 10, 10, 0, 0, ZoneOffset.of('+01:00'))
        DateTimeTesting dt = new DateTimeTesting(
                createdAtEpochSeconds: Instant.now().epochSecond,
                jodaDateTime: jodaDateTime,
                javaDateTime: javaDateTime
        )
        dt.save(flush: true)

        when:
        String sql = """
select  DATE_FORMAT(joda_date_time, '%Y-%m-%d %H:%i') as jodaDatetime,
        DATE_FORMAT(java_date_time, '%Y-%m-%d %H:%i') as javaDatetime
from date_time_testing
where id = :id
"""
        def sqlInstance = new Sql(dataSource)
        // query -> SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;
        String timezone = sqlInstance.firstRow("SELECT @@GLOBAL.time_zone as globalTz, @@SESSION.time_zone as sessionTz")
        LOG.info "SQL Instance Timezone: " + timezone

        List<GroovyRowResult> results = sqlInstance.rows(sql, [id: dt.id])
        String jodaDateTimeString = results.find().getProperty('jodaDatetime')
        String javaDateTimeString = results.find().getProperty('javaDatetime')
        LOG.info "1) javaDateTimeString (${javaDateTimeString}) == '2024-01-01 10:10' -> " + (javaDateTimeString == '2024-01-01 10:10').toString()
        LOG.info "2) jodaDateTimeString (${jodaDateTimeString}) == '2024-01-01 10:10' -> " + (jodaDateTimeString == '2024-01-01 10:10').toString()

        then:

        javaDateTimeString == '2024-01-01 10:10'
        jodaDateTimeString == '2024-01-01 10:10'
    }

    void "GORM readback yields the exact same objects"() {
        given:
        DateTime jodaDateTime = new DateTime(2024, 1, 1, 10, 10, DateTimeZone.forID('+01:00'))
        OffsetDateTime javaDateTime = OffsetDateTime.of(2024, 1, 1, 10, 10, 0, 0, ZoneOffset.of('+01:00'))
        DateTimeTesting dt = new DateTimeTesting(
                createdAtEpochSeconds: Instant.now().epochSecond,
                jodaDateTime: jodaDateTime,
                javaDateTime: javaDateTime
        )
        dt.save(flush: true)

        when:
        DateTimeTesting found = DateTimeTesting.get(dt.id)
        LOG.info "1) found.javaDateTime (${found.javaDateTime}) == ${javaDateTime} -> " + (found.javaDateTime == javaDateTime).toString()
        LOG.info "2) found.jodaDateTime (${found.jodaDateTime}) == ${jodaDateTime} -> " + (found.jodaDateTime == jodaDateTime).toString()

        then:
        found.jodaDateTime == jodaDateTime
        found.javaDateTime == javaDateTime
    }
}
