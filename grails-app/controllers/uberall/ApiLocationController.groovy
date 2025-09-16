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

    def beforeInterceptor = {
        JSON.use('v1')
        return true
    }

    @Override
    protected Location queryForResource(Serializable id) {
        Location.where { id == id }.get()
    }

    @Override
    protected List<Location> listAllResources(Map params) {
        Location.list(params)
    }

    // GET /api/v1/locations/
    def search() {
        respond listAllResources(params)
    }

    // POST /api/v1/locations/
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

    // DELETE /api/v1/locations/
    @Transactional
    def deleteLocations() {
        // Bulk delete operation
        def deletedCount = Location.executeUpdate("delete from Location")
        render status: 200, text: "Deleted ${deletedCount} locations"
    }

    // PUT /api/v1/locations/
    @Transactional
    def editLocations() {
        // Bulk edit operation - implementation depends on requirements
        respond([message: 'Bulk edit not implemented'], status: 501)
    }

    // GET /api/v1/locations/{id}
    @Override
    def show() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }
        respond instance
    }

    // PUT /api/v1/locations/{id}
    @Transactional
    @Override
    def update() {
        def instance = queryForResource(params.id)
        if (!instance) {
            render status: 404
            return
        }

        instance.properties = getObjectToBind()
        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, status: 422
            return
        }
        instance.save flush:true
        respond instance, status: 200
    }

    // DELETE /api/v1/locations/{id}
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
        bindData instance, getObjectToBind()
        instance
    }
}
