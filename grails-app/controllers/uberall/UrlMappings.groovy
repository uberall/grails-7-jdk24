package uberall

class UrlMappings {

    static mappings = {
        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

        "/admin/listing/$action/$id?"(controller: "adminListing")
        "/admin/location/$action/$id?"(controller: "adminLocation")

        // Existing API routes
        group("/api") {
            group("/locations") {
                "/"(controller: 'apiLocation', action: 'search', method: 'GET')
                "/"(controller: 'apiLocation', action: "create", method: 'POST')
                "/"(controller: 'apiLocation', action: "deleteLocations", method: 'DELETE')
                "/"(controller: 'apiLocation', action: "editLocations", method: 'PUT')
                "/$id"(controller: 'apiLocation', action: 'show', method: 'GET')
                "/$id"(controller: 'apiLocation', action: 'update', method: 'PUT')
                "/$id"(controller: 'apiLocation', action: 'delete', method: 'DELETE')
            }
            group("/listings") {
                "/"(controller: 'apiListing', action: 'index', method: 'GET')
                "/"(controller: 'apiListing', action: "save", method: 'POST')
                "/$id"(controller: 'apiListing', action: 'show', method: 'GET')
                "/$id"(controller: 'apiListing', action: 'update', method: 'PUT')
                "/$id"(controller: 'apiListing', action: 'delete', method: 'DELETE')
            }
        }
    }
}
