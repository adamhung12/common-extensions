package me.xethh.libs.extension.set.sst.core.FirstLayerFilter

import me.xethh.libs.extension.set.core.EnableSETCore
import me.xethh.libs.toolkits.logging.WithLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.GenericFilterBean
import javax.annotation.PostConstruct
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Import(EnableFirstLayerFilterConfig::class)
@EnableSETCore
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EnableFirstLayerFilter

@EnableConfigurationProperties(FirstLayerFilterConfigProperties::class)
class EnableFirstLayerFilterConfig : WithLogger{
    @Bean
    fun firstLayerFilter():FirstLayerFilter{
        return FirstLayerFilter()
    }


    @PostConstruct
    fun init(){
        logger().info("Init first filter completed")
    }

}

@ConfigurationProperties(prefix = "sst-core")
class FirstLayerFilterConfigProperties{

}

@Order(Ordered.HIGHEST_PRECEDENCE)
class FirstLayerFilter : GenericFilterBean(), WithLogger {
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, filterChain : FilterChain?) {
        logger.debug("New request")
        request?.let{
            logger.debug("Request from ${request.remoteHost}:${request.remoteAddr}:${request.remotePort} to ${request.localName}:${request.localAddr}:${request.localPort}")
            filterChain?.doFilter(request, response)
        }
    }
}