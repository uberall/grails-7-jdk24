package uberall

import grails.gorm.transactions.Transactional
import uberall.api.AbstractApiController
import uberall.api.util.ResponseUtil

@Transactional(readOnly = true)
class ApiLocationController extends AbstractApiController {
    static responseFormats = ['json']
    static allowedFields = ['name', 'address', 'valid']

    // GET /api/locations/
    def index() {
        def locations = Location.list()
        renderJson(ResponseUtil.getSuccess([locations: locations]))
    }

    // POST /api/locations/
    @Transactional
    def save() {
        def jsonData = request.JSON
        def instance = new Location()

        bindData(instance, jsonData, [include: allowedFields])
        instance.validate()
        if (instance.hasErrors()) {
            renderErrors(instance)
            return
        }
        instance.save flush:true
        renderJson(ResponseUtil.getSuccess([location: instance]))
    }

    // DELETE /api/locations/
    @Transactional
    def deleteLocations() {
        // Bulk delete operation
        def deletedCount = Location.executeUpdate("delete from Location")
        renderJson(ResponseUtil.getSuccess([deletedCount: deletedCount]))
    }

    // PUT /api/locations/
    @Transactional
    def editLocations() {
        // Bulk edit operation - implementation depends on requirements
        renderJson(ResponseUtil.getError('Bulk edit not implemented'))
    }

    // GET /api/locations/{id}
    def show() {
        if (!params.id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
            return
        }

        def instance = Location.get(params.id as Long)
        if (!instance) {
            renderJson(ResponseUtil.getNotFound('Location not found'))
            return
        }
        renderJson(ResponseUtil.getSuccess([location: instance]))
    }

    // PUT /api/locations/{id}
    @Transactional
    def update() {
        if (!params.id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
            return
        }

        def instance = Location.get(params.id as Long)
        if (!instance) {
            renderJson(ResponseUtil.getNotFound('Location not found'))
            return
        }

        def jsonData = request.JSON

        bindData(instance, jsonData, [include: allowedFields])
        instance.validate()
        if (instance.hasErrors()) {
            instance.discard()
            renderErrors(instance)
            return
        }
        instance.save flush:true
        renderJson(ResponseUtil.getSuccess([location: instance]))
    }

    // DELETE /api/locations/{id}
    @Transactional
    def delete() {
        if (!params.id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
            return
        }

        def instance = Location.get(params.id as Long)
        if (!instance) {
            renderJson(ResponseUtil.getNotFound('Location not found'))
            return
        }
        try {
            instance.delete flush:true
            renderJson(ResponseUtil.getSuccess([deleted: true]))
        } catch (Exception e) {
            renderJson(ResponseUtil.getError('Cannot delete location with associated listings'))
        }
    }
}
