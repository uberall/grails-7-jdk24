package uberall

import grails.gorm.transactions.Transactional
import uberall.api.AbstractApiController
import uberall.api.util.ResponseUtil

@Transactional(readOnly = true)
class ApiListingController extends AbstractApiController {
    static responseFormats = ['json']
    static allowedFields = ['directory', 'status']

    // GET /api/listings/
    def index() {
        def listings = Listing.list()
        renderJson(ResponseUtil.getSuccess([listings: listings]))
    }

    // POST /api/listings/
    @Transactional
    def save() {
        def jsonData = request.JSON

        def instance = new Listing()

        // Resolve location explicitly and ensure 404 for non-existent id
        if (jsonData.location?.id) {
            def location = Location.get(jsonData.location.id as Long)
            if (!location) {
                renderJson(ResponseUtil.getNotFound('Location not found'))
                return
            }
            instance.location = location
        }

        bindData(instance, jsonData, [include: allowedFields])
        instance.validate()
        if (instance.hasErrors()) {
            renderErrors(instance)
            return
        }
        instance.save flush:true
        renderJson(ResponseUtil.getSuccess([listing: instance]))
    }

    // GET /api/listings/{id}
    def show() {
        if (!params.id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
            return
        }

        def instance = Listing.get(params.id as Long)
        if (!instance) {
            renderJson(ResponseUtil.getNotFound('Listing not found'))
            return
        }
        renderJson(ResponseUtil.getSuccess([listing: instance]))
    }

    // PUT /api/listings/{id}
    @Transactional
    def update() {
        if (!params.id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
            return
        }

        def instance = Listing.get(params.id as Long)
        if (!instance) {
            renderJson(ResponseUtil.getNotFound('Listing not found'))
            return
        }

        def jsonData = request.JSON

        if (jsonData.location?.id) {
            def location = Location.get(jsonData.location.id as Long)
            if (!location) {
                renderJson(ResponseUtil.getNotFound('Location not found'))
                return
            }
            instance.location = location
        }

        bindData(instance, jsonData, [include: allowedFields])
        instance.validate()
        if (instance.hasErrors()) {
            instance.discard()
            renderErrors(instance)
            return
        }
        instance.save flush:true
        renderJson(ResponseUtil.getSuccess([listing: instance]))
    }

    // DELETE /api/listings/{id}
    @Transactional
    def delete() {
        if (!params.id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
            return
        }

        def instance = Listing.get(params.id as Long)
        if (!instance) {
            renderJson(ResponseUtil.getNotFound('Listing not found'))
            return
        }
        instance.delete flush:true
        renderJson(ResponseUtil.getSuccess([deleted: true]))
    }
}
