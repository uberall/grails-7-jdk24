package uberall

import grails.rest.RestfulController
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ApiListingController extends RestfulController<Listing> {
    static responseFormats = ['json']
    static allowedFields = ['directory', 'status', 'location']

    ApiListingController() {
        super(Listing)
    }

    def beforeInterceptor = {
        JSON.use('v1')
        return true
    }

    @Override
    protected Listing queryForResource(Serializable id) {
        Listing.where { id == id }.get()
    }

    @Override
    protected List<Listing> listAllResources(Map params) {
        Listing.list(params)
    }

    // GET /api/v1/listings/
    @Override
    def index() {
        respond listAllResources(params)
    }

    // POST /api/v1/listings/
    @Transactional
    @Override
    def save() {
        def instance = createResource()
        
        if (request.JSON.location?.id) {
            instance.location = Location.get(request.JSON.location.id)
            if (!instance.location) {
                respond([error: 'Location not found'], status: 404)
                return
            }
        }
        
        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, status: 422
            return
        }
        
        instance.save flush:true
        respond instance, [status: 201]
    }

    // GET /api/v1/listings/{id}
    @Override
    def show() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        respond instance
    }

    // PUT /api/v1/listings/{id}
    @Transactional
    @Override
    def update() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        
        def jsonData = request.JSON
        
        if (jsonData.location?.id) {
            def location = Location.get(jsonData.location.id)
            if (!location) {
                respond([error: 'Location not found'], status: 404)
                return
            }
            instance.location = location
        }
        
        bindData(instance, jsonData, [include: ['directory', 'status']])
        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, status: 422
            return
        }
        instance.save flush:true
        respond instance, status: 200
    }

    // DELETE /api/v1/listings/{id}
    @Transactional
    @Override
    def delete() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        instance.delete flush:true
        render status: 204
    }

    @Override
    protected Listing createResource() {
        Listing instance = resource.newInstance()
        def jsonData = request.JSON
        
        if (jsonData.location?.id) {
            instance.location = Location.get(jsonData.location.id)
        }
        
        bindData(instance, jsonData, [include: ['directory', 'status']])
        instance
    }
}
