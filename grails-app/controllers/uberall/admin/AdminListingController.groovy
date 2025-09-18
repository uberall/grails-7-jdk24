package uberall.admin

import grails.gorm.transactions.Transactional
import uberall.Listing
import uberall.ListingDataService

import static org.springframework.http.HttpStatus.*

class AdminListingController {

    ListingDataService listingDataService

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        log.info("Number of listings: ${Listing.count()}")
        respond Listing.list(params), model:[listingCount: Listing.count()]
    }

    def show(Listing listing) {
        respond listing
    }

    def create() {
        Listing listing = new Listing()
        listing.directory = params.directory
        listing.status = params.status
        listing.location = params.location

        respond listing
    }

    @Transactional
    def save(Listing listing) {
        if (listing == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (listing.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond listing.errors, view:'create'
            return
        }

        try {
            listingDataService.save(listing)
        } catch (IllegalArgumentException e) {
            render e.message, status: BAD_REQUEST
        } catch (Exception e) {
            render e.message, status: INTERNAL_SERVER_ERROR
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'listing.label', default: 'Listing'), listing.id])
                redirect uri: '/admin/listing/show/' + listing.id
            }
            '*' { respond listing, [status: CREATED] }
        }
    }

    //Get("/{id}/edit")
    def edit(Listing listing) {
        respond listing
    }

    //Put("/{id}")
    @Transactional
    def update(Listing listing) {
        if (listing == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (listing.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond listing.errors, view:'edit'
            return
        }

        listing.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'listing.label', default: 'Listing'), listing.id])
                redirect uri: '/admin/listing/show/' + listing.id
            }
            '*' { respond listing, [status: OK] }
        }
    }

    //Delete("/{id}")
    @Transactional
    def delete(Listing listing) {
        if (listing == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        listing.delete flush:true
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'listing.label', default: 'Listing'), listing.id])
                // Use explicit mapped URI so redirectedUrl matches '/admin/listing/index'
                redirect uri: '/admin/listing/index'
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'listing.label', default: 'Listing'), params.id])
                // Use explicit mapped URI to ensure consistent redirect path
                redirect uri: '/admin/listing/index'
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
