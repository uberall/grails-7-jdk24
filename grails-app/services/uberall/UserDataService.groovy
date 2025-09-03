package uberall

import grails.gorm.services.Service

@Service(User)
interface UserDataService {
    User find(Serializable id)
    User save(User user)
    void delete(Serializable id)
}
