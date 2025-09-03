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

@Integration
@Transactional
class DateTimeTestingIntegrationSpec extends Specification {
    DataSource dataSource

    void "joda DateTime and java OffsetDateTime are being stored correctly"() {
        when: 'we expect the servers to be in UTC+01:00 timezone for set datetime 2024-01-01 10:10:00'
        TimeZone defaultTz = TimeZone.default
        TimeZone.setDefault(TimeZone.getTimeZone('Europe/Berlin'))
        DateTime jodaDateTime = new DateTime(
                2024, 1, 1, 10, 10,
                DateTimeZone.forID('+01:00')
        )
        OffsetDateTime javaDateTime = OffsetDateTime.of(
                2024, 1, 1, 10, 10, 0, 0,
                ZoneOffset.of("+01:00")
        )

        and:
        DateTimeTesting dateTimeTesting = new DateTimeTesting()
        dateTimeTesting.createdAtEpochSeconds = Instant.now().epochSecond
        dateTimeTesting.jodaDateTime = jodaDateTime
        dateTimeTesting.javaDateTime = javaDateTime
        dateTimeTesting.save(flush: true)

        then:
        String sql = """
select  DATE_FORMAT(joda_date_time, '%Y-%m-%d %H:%i') as jodaDatetime,
        DATE_FORMAT(java_date_time, '%Y-%m-%d %H:%i') as javaDatetime
from date_time_testing
where id = :id
"""
        List<GroovyRowResult> results = new Sql(dataSource).rows(sql, [id: dateTimeTesting.id])
        String jodaDateTimeString =  results.find().getProperty('jodaDatetime')
        String javaDateTimeString =  results.find().getProperty('javaDatetime')

        and: 'we expect the same local Berlin time stored (no UTC shift applied)'
        javaDateTimeString == '2024-01-01 10:10'
        jodaDateTimeString == '2024-01-01 10:10'

        and: 'dates are being read and parsed correctly'
        DateTimeTesting found = DateTimeTesting.get(dateTimeTesting.id)
        found.jodaDateTime == jodaDateTime
        found.javaDateTime == javaDateTime

        cleanup:
        TimeZone.setDefault(defaultTz)
    }
}
