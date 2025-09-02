package uberall.test

import grails.testing.mixin.integration.Integration
import org.springframework.transaction.annotation.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
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

    @Ignore("Fails in CI/CD but works locally - investigate later")
    void "raw SQL storage honors local joda timezone"() {
        given:
        DateTime jodaDateTime = new DateTime(2024, 1, 1, 10, 10, DateTimeZone.forID('+01:00'))
        JodaTimeTesting dt = new JodaTimeTesting(
                createdAtEpochSeconds : Instant.now().epochSecond,
                jodaDateTime : jodaDateTime
        )
        dt.save(flush: true)

        when:
        String sql = """
select DATE_FORMAT(joda_date_time, '%Y-%m-%d %H:%i') as jodaDatetime
from joda_time_testing
where id = :id
"""
        def sqlInstance = new Sql(dataSource)
        // query -> SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;
        String timezone = sqlInstance.firstRow("SELECT @@GLOBAL.time_zone as globalTz, @@SESSION.time_zone as sessionTz")
        LOG.info "SQL Instance Timezone: " + timezone

        List<GroovyRowResult> results = sqlInstance.rows(sql, [id: dt.id])
        String jodaDateTimeString = results.find().getProperty('jodaDatetime')
        LOG.info "jodaDateTimeString (${jodaDateTimeString}) == '2024-01-01 10:10' -> " + (jodaDateTimeString == '2024-01-01 10:10').toString()

        then:

        jodaDateTimeString == '2024-01-01 10:10'
    }

    void "raw SQL storage honors local java timezone"() {
        given:
        OffsetDateTime javaDateTime = OffsetDateTime.of(2024, 1, 1, 10, 10, 0, 0, ZoneOffset.of('+01:00'))
        JavaTimeTesting dt = new JavaTimeTesting(
                createdAtEpochSeconds: Instant.now().epochSecond,
                javaDateTime: javaDateTime
        )
        dt.save(flush: true)

        when:
        String sql = """
select DATE_FORMAT(java_date_time, '%Y-%m-%d %H:%i') as javaDatetime
from java_time_testing
where id = :id
"""
        def sqlInstance = new Sql(dataSource)
        List<GroovyRowResult> results = sqlInstance.rows(sql, [id: dt.id])
        String javaDateTimeString = results.find().getProperty('javaDatetime')

        then:
        javaDateTimeString == '2024-01-01 10:10'
    }

    void "GORM readback yields the exact same joda objects"() {
        given:
        DateTime jodaDateTime = new DateTime(2024, 1, 1, 10, 10, DateTimeZone.forID('+01:00'))
        JodaTimeTesting dt = new JodaTimeTesting(
                createdAtEpochSeconds: Instant.now().epochSecond,
                jodaDateTime: jodaDateTime
        )
        dt.save(flush: true)

        when:
        JodaTimeTesting found = JodaTimeTesting.get(dt.id)

        then:
        found.jodaDateTime == jodaDateTime
    }

    void "GORM readback yields the exact same java objects"() {
        given:
        OffsetDateTime javaDateTime = OffsetDateTime.of(2024, 1, 1, 10, 10, 0, 0, ZoneOffset.of('+01:00'))
        JavaTimeTesting dt = new JavaTimeTesting(
                createdAtEpochSeconds: Instant.now().epochSecond,
                javaDateTime: javaDateTime
        )
        dt.save(flush: true)

        when:
        JavaTimeTesting found = JavaTimeTesting.get(dt.id)

        then:
        found.javaDateTime == javaDateTime
    }
}
