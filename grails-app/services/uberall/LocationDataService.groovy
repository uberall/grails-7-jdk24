package uberall

import grails.gorm.services.Service

@Service(Location)
interface LocationDataService {
    Location find(Serializable id)
    Location save(Location location)
    void delete(Serializable id)
}
