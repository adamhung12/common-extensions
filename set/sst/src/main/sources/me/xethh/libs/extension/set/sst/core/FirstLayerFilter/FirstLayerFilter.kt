package me.xethh.libs.extension.set.sst.core.FirstLayerFilter

import me.xethh.libs.extension.set.core.AppMeta
import me.xethh.libs.extension.set.core.EnableSETCore
import me.xethh.libs.extension.set.core.RequestMeta
import me.xethh.libs.extension.set.core.RequestMetaImpl
import me.xethh.libs.extension.set.core.idProvider.IdProvider
import me.xethh.libs.extension.set.sst.core.FirstLayerFilter.internalFilter.InternalFilter
import me.xethh.libs.extension.set.sst.core.FirstLayerFilter.internalFilter.InternalFilterChain
import me.xethh.libs.extension.set.sst.core.FirstLayerFilter.requestFilter.*
import me.xethh.libs.toolkits.logging.WithLogger
import me.xethh.libs.toolkits.webDto.core.MetaEntity
import me.xethh.libs.toolkits.webDto.core.WebBaseEntity
import org.apache.catalina.core.ApplicationContext
import org.apache.commons.lang3.StringUtils
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.GenericFilterBean
import java.lang.RuntimeException
import javax.annotation.PostConstruct
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Import(EnableFirstLayerFilterConfig::class)
@EnableSETCore
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EnableFirstLayerFilter

@EnableConfigurationProperties(FirstLayerFilterConfigProperties::class)
class EnableFirstLayerFilterConfig : WithLogger{
    @Autowired lateinit var firstFilterConfigurationProperties: FirstLayerFilterConfigProperties

    @Bean fun internalFilterChain(@Autowired(required = false) listOfFilter : List<InternalFilter>?):InternalFilterChain{
        val list = listOfFilter ?: listOf()
        return InternalFilterChain(list)
    }

    @Bean fun sSTRequestURLVerifier(@Autowired firstLayerFilterConfigProperties: FirstLayerFilterConfigProperties):SSTRequestURLVerifier{
        val filterList = firstLayerFilterConfigProperties.requestFilter.map {
            if(it.toLowerCase().startsWith("static:")){
                StaticSSTRequestURLFilter(it.substring(7))
            }
            else if(it.toLowerCase().startsWith("regex:")){
                RegSSTRequestURLFilter(it.substring(6))
            }
            else throw RuntimeException("URLFilter[$it] not support")
        }
        return SSTRequestURLVerifier(filterList)
    }


    @Bean
    fun firstLayerFilter():FirstLayerFilter{
        return FirstLayerFilter()
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun requestMeta(
            @Autowired idProvider: IdProvider,
            @Autowired httpServletRequest: HttpServletRequest
    ):RequestMeta{
        val id:String = httpServletRequest.getParameter(MetaEntity.HEADER.REQUEST_ID_HEADER)?:idProvider.gen()
        return RequestMetaImpl(httpServletRequest.getSession(true).id, id, AppMeta.metaEntity(httpServletRequest))
    }


    @PostConstruct
    fun init(){
        logger().info("Init first filter completed")
    }

}

@ConfigurationProperties(prefix = "sst-core")
class FirstLayerFilterConfigProperties{
    lateinit var requestFilter : List<String>
}

@Order(Ordered.HIGHEST_PRECEDENCE)
class FirstLayerFilter : GenericFilterBean(), WithLogger {
    @Autowired lateinit var requestMeta:RequestMeta
    @Autowired lateinit var sSTRequestURLVerifier:SSTRequestURLVerifier
    @Autowired lateinit var internalFilterChain: InternalFilterChain

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, filterChain : FilterChain?) {
        logger.debug("New request")
        request?.let{
            if(request is HttpServletRequest && response is HttpServletResponse && sSTRequestURLVerifier.verify(VerifyingRequest(request))){
                logger.info("received request for SST")
                logger.debug(VerifyingRequest(request))
                val res = internalFilterChain.filter(request, response)
                filterChain?.doFilter(res.first, res.second)
            }
            else
                filterChain?.doFilter(request, response)
        }
    }
}