package uberall

import grails.rest.RestfulController
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ApiLocationController extends RestfulController<Location> {
    static responseFormats = ['json']
    static allowedFields = ['name', 'address', 'valid']

    ApiLocationController() {
        super(Location)
    }

    @Override
    protected Location queryForResource(Serializable id) {
        if (id == null) {
            return null
        }
        // Use direct lookup by primary key to avoid variable shadowing issues
        Location.get(id as Long)
    }

    @Override
    protected List<Location> listAllResources(Map params) {
        Location.list(params)
    }

    // GET /api/locations/
    def index() {
        respond listAllResources(params)
    }

    // POST /api/locations/
    @Transactional
    def create() {
        def instance = createResource()
        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, status: 422
            return
        }
        instance.save flush:true
        respond instance, [status: 201]
    }

    // DELETE /api/locations/
    @Transactional
    def deleteLocations() {
        // Bulk delete operation
        def deletedCount = Location.executeUpdate("delete from Location")
        render status: 200, text: "Deleted ${deletedCount} locations"
    }

    // PUT /api/locations/
    @Transactional
    def editLocations() {
        // Bulk edit operation - implementation depends on requirements
        respond([message: 'Bulk edit not implemented'], status: 501)
    }

    // GET /api/locations/{id}
    @Override
    def show() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        respond instance
    }

    // PUT /api/locations/{id}
    @Transactional
    @Override
    def update() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }

        def jsonData = request.JSON

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

    // DELETE /api/locations/{id}
    @Transactional
    @Override
    def delete() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        try {
            instance.delete flush:true
            render status: 204
        } catch (Exception e) {
            respond([error: 'Cannot delete location with associated listings'], status: 409)
        }
    }

    @Override
    protected Location createResource() {
        Location instance = resource.newInstance()
        def jsonData = request.JSON

        bindData(instance, jsonData, [include: allowedFields])
        instance
    }
}
