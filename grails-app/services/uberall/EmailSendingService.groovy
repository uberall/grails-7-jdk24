package uberall

import grails.plugins.mail.MailService

class EmailSendingService {

    MailService mailService

    void sendEmail(String to, String subject, String body) {
        mailService.sendMail {
            to to
            subject subject
            body body
        }
    }
}
