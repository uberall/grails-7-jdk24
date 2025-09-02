package uberall

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    UserDataService userDataService

    void create(String email) {
        User user = new User()
        user.email = email
        userDataService.save(user, flush: true)
    }

    User get(Long id) {
        userDataService.find(id)
    }
}
