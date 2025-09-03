package uberall

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }
        "/swagger/$action?/$id?"(controller: "swagger", action: "index")

        "/api/v1/listings"(resources: "listing")
        "/api/v1/locations"(resources: "location")
        "/api/v1/validate/$id"(controller: "location", action: "validate")

        "/admin/jesque/"(controller: 'jesqueAdmin', action: 'index')
        "/admin/jesque/api/overview"(controller: 'jesqueAdmin', action: 'overview')
        "/admin/jesque/api/queues"(controller: 'jesqueAdminQueue', action: 'list')
        "/admin/jesque/api/queues/$name"(controller: 'jesqueAdminQueue', action: 'details', method: "GET")
        "/admin/jesque/api/queues/$name"(controller: 'jesqueAdminQueue', action: 'remove', method: "DELETE")
        "/admin/jesque/api/jobs"(controller: 'jesqueAdminStatistics', action: 'jobs', method: "GET")
        "/admin/jesque/api/jobs"(controller: 'jesqueAdminJob', action: 'enqueue', method: "POST")
        "/admin/jesque/api/jobs/removeDelayed"(controller: 'jesqueAdminJob', action: 'removeDelayed', method: "POST")
        "/admin/jesque/api/jobs/failed"(controller: 'jesqueAdminJob', action: 'failed', method: "GET")
        "/admin/jesque/api/jobs/failed"(controller: 'jesqueAdminJob', action: 'clear', method: "DELETE")
        "/admin/jesque/api/jobs/failed/$id"(controller: 'jesqueAdminJob', action: 'retry', method: "POST")
        "/admin/jesque/api/jobs/failed/$id"(controller: 'jesqueAdminJob', action: 'remove', method: "DELETE")
        "/admin/jesque/api/jobs/triggers"(controller: 'jesqueAdminJob', action: 'triggers', method: "GET")
        "/admin/jesque/api/jobs/triggers/$name"(controller: 'jesqueAdminJob', action: 'deleteTrigger', method: "DELETE")
        "/admin/jesque/api/jobs/$job"(controller: 'jesqueAdminStatistics', action: 'list', method: "GET")
        "/admin/jesque/api/workers"(controller: 'jesqueAdminWorker', action: 'list', method: 'GET')
        "/admin/jesque/api/workers"(controller: 'jesqueAdminWorker', action: 'manual', method: 'POST')
        "/admin/jesque/api/workers/$name"(controller: 'jesqueAdminWorker', action: 'remove', method: 'DELETE')
        "/admin/jesque/api/workers/pause"(controller: 'jesqueAdminWorker', action: 'pauseAll', method: 'GET')
        "/admin/jesque/api/workers/resume"(controller: 'jesqueAdminWorker', action: 'resumeAll', method: 'GET')
        "/admin/jesque/api/workers/pause/$channel"(controller: 'jesqueAdminWorker', action: 'pauseChannel', method: 'GET')
        "/admin/jesque/api/workers/resume/$channel"(controller: 'jesqueAdminWorker', action: 'resumeChannel', method: 'GET')
        "/admin/login"(controller: "adminAuthentication", action: "login")
        "/admin/oauth"(controller: "adminAuthentication", action: "oauthCallback")

        // OAuth URLs
        "/oauth/$provider/success"(controller: "OAuth", action: 'success')
        "/oauth/$provider/failure"(controller: "OAuth", action: 'failure')
        "/oauth/$provider/callback"(controller: "OAuth", action: 'callback')
        "/oauth/$provider/finish"(controller: "OAuth", action: 'finish')
        "/oauth/googleCallback"(controller: 'googleConnect', action: 'callback')
        "/oauth/facebookCallback"(controller: 'facebookConnect', action: 'callback')
        "/oauth/twitterCallback"(controller: 'twitterConnect', action: 'callback')

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
