package uberall

import jakarta.servlet.ServletContext

class BootStrap {

    ServletContext servletContext
    Converters converterBean

    def init = { ServletContext ctx ->
        converterBean.init()
    }

    def destroy = {
    }

}
