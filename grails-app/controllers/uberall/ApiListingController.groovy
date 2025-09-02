package uberall

import grails.rest.RestfulController
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ApiListingController extends RestfulController<Listing> {
    static responseFormats = ['json']
    static allowedFields = ['directory', 'status']

    ApiListingController() {
        super(Listing)
    }

    @Override
    protected Listing queryForResource(Serializable id) {
        // Use direct get to avoid closure scope issues (id == id always true)
        Listing.get(id as Long)
    }

    @Override
    protected List<Listing> listAllResources(Map params) {
        Listing.list(params)
    }

    // GET /api/listings/
    @Override
    def index() {
        respond listAllResources(params)
    }

    // POST /api/listings/
    @Transactional
    @Override
    def save() {
        def instance = createResource()

        def jsonData = request.JSON

        // Resolve location explicitly and ensure 404 for non-existent id
        if (jsonData.location?.id) {
            def location = Location.get(jsonData.location.id as Long)
            if (!location) {
                respond([error: 'Location not found'], status: 404)
                return
            }
            instance.location = location
        }

        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, status: 422
            return
        }
        instance.save flush:true
        respond instance, [status: 201]
    }

    // GET /api/listings/{id}
    @Override
    def show() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        respond instance
    }

    // PUT /api/listings/{id}
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
            def location = Location.get(jsonData.location.id as Long)
            if (!location) {
                respond([error: 'Location not found'], status: 404)
                return
            }
            instance.location = location
        }

        bindData(instance, jsonData, [include: allowedFields])
        instance.validate()
        if (instance.hasErrors()) {
            instance.discard()
            respond instance.errors, status: 422
            return
        }
        instance.save flush:true
        respond instance, status: 200
    }

    // DELETE /api/listings/{id}
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

        bindData(instance, jsonData, [include: allowedFields])
        instance
    }
}
