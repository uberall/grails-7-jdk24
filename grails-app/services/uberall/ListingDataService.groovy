package uberall

import grails.gorm.services.Service

@Service(Listing)
interface ListingDataService {
    Listing save(Listing listing)
    void delete(Serializable id)
}
