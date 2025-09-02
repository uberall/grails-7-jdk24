package uberall

import grails.plugins.mail.MailService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class EmailSendingServiceSpec extends Specification implements ServiceUnitTest<EmailSendingService> {

    void "test sendEmail"() {
        given:
        def mailService = Mock(MailService)
        service.mailService = mailService

        when:
        service.sendEmail("test@example.com", "Test Subject", "Test Body")

        then:
        1 * mailService.sendMail(_)
    }
}
