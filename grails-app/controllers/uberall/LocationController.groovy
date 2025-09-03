package uberall

import grails.gorm.transactions.Transactional

import static org.springframework.http.HttpStatus.*

class LocationController {

    LocationService locationService

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Location.list(params), model:[locationCount: Location.count()]
    }

    def create() {
        Location location = new Location()
        location.name = params.name
        location.address = params.address

        respond location
    }

    def show(Location location) {
        respond location
    }

    //Get("/{id}/edit")
    def edit(Location location) {
        respond location
    }

    @Transactional
    def save(Location location) {
        if (location == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (location.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond location.errors, view:'create'
            return
        }

        try {
            locationService.save(location)
        } catch (IllegalArgumentException e) {
            render e.message, status: BAD_REQUEST
            return
        } catch (Exception e) {
            render e.message, status: INTERNAL_SERVER_ERROR
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'location.label', default: 'Location'), location.id])
                redirect location
            }
            '*' { respond location, [status: CREATED] }
        }
    }

    @Transactional
    def update(Location location) {
        if (location == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (location.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond location.errors, view:'edit'
            return
        }

        location.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'location.label', default: 'Location'), location.id])
                redirect location
            }
            '*' { respond location, [status: OK] }
        }
    }

    //Delete("/{id}")
    @Transactional
    def delete(Location location) {
        if (location == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        location.delete flush:true
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'location.label', default: 'Location'), location.id])
                redirect action:"index", method:"GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
