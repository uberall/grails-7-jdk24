import uberall.Converters

beans = {
    converterBean(Converters) { bean ->
        bean.autowire = 'byName'
    }
}