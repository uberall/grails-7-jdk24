package uberall.test

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfiguration {

    @PostConstruct
    void configureJadiraTimezone() {
        // Set Jadira usertype properties as system properties
        System.setProperty('jadira.usertype.databaseZone', '+01:00')
        System.setProperty('jadira.usertype.javaZone', 'jvm')
        System.setProperty('jadira.usertype.autoRegisterUserTypes', 'true')
    }
}
