package uberall

import java.time.ZonedDateTime

class User {
    Long id
    Long version
    ZonedDateTime dateCreated
    ZonedDateTime lastUpdated

    String email

    static mapping = {
        dateCreated sqlType: 'datetime' //just an example so we know which sqlType we need to use, auto would be "datetime(6) which also stores ms
        lastUpdated sqlType: 'datetime' //just an example so we know which sqlType we need to use, auto would be "datetime(6) which also stores ms
    }

    @Override
    String toString() {
        [
                id         : id,
                version    : version,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
                email      : email
        ]
    }
}
