package me.xethh.libs.extension.set.sst.core.FirstLayerFilter

import me.xethh.libs.extension.set.core.EnableSETCore
import me.xethh.libs.toolkits.logging.WithLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Import(EnableFirstLayerFilterConfig::class)
@EnableSETCore
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EnableFirstLayerFilter

@EnableConfigurationProperties(FirstLayerFilterConfigProperties::class)
class EnableFirstLayerFilterConfig{

}

@ConfigurationProperties(prefix = "sst-core")
class FirstLayerFilterConfigProperties{

}

@Order(Ordered.HIGHEST_PRECEDENCE)
class FirstLayerFilter : GenericFilterBean(), WithLogger {
    override fun doFilter(p0: ServletRequest?, p1: ServletResponse?, p2: FilterChain?) {
        logger.debug("New request")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}