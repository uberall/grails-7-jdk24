import uberall.Converters

beans = {
    converterBean(Converters) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
    }
}