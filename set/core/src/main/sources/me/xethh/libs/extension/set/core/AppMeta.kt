package me.xethh.libs.extension.set.core

import me.xethh.libs.extension.set.core.idProvider.MachineBasedProvider
import me.xethh.libs.extension.set.core.idProvider.TimeBasedProvider
import me.xethh.libs.extension.set.core.appProvider.AppNameProvider
import me.xethh.libs.extension.set.core.appProvider.DefaultAppNameProvider
import me.xethh.libs.extension.set.core.appProvider.NoneAppNameProvider
import me.xethh.libs.extension.set.core.idProvider.IdProvider
import me.xethh.libs.toolkits.logging.WithLogger
import me.xethh.libs.toolkits.webDto.core.MetaEntity
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.util.*
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

@Import(SETCoreConfig::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EnableSETCore

@EnableConfigurationProperties(SETCoreConfigProperties::class)
open class SETCoreConfig : WithLogger{
    @Bean
    open fun appNameProvider(): AppNameProvider {
        return when (SETCoreConfigProperties.appName.type!!) {
            AppNameConfig.BuildType.Default -> DefaultAppNameProvider()
            AppNameConfig.BuildType.None -> NoneAppNameProvider()
            AppNameConfig.BuildType.Custom -> throw RuntimeException("app name provider not supported")
        }
    }

    @Bean
    open fun appMeta(): AppMeta {
        return AppMeta()
    }

    @Bean
    open fun idProvider(@Autowired appMeta:AppMeta): IdProvider {
        return when (SETCoreConfigProperties.idProvider.type!!){
            IdProviderConfig.ProviderType.MachineBase -> {
                val mbp = MachineBasedProvider()
                mbp.serviceId = appMeta.appNameProvider.gen()
                mbp
            }
            IdProviderConfig.ProviderType.TimeBase -> TimeBasedProvider()
            IdProviderConfig.ProviderType.Custom -> throw RuntimeException("Custom id provider not supported")
        }
    }

    @Autowired lateinit var SETCoreConfigProperties: SETCoreConfigProperties
    @Autowired lateinit var appMeta: AppMeta
    @Autowired lateinit var appNameProvider: AppNameProvider
//    @Autowired lateinit var idProvider: IdProvider

    @PostConstruct
    fun init(){
        logger().info("-------------------------------")
        logger().info("Init SET complete")
        logger().info("AppName: ${appMeta.appNameProvider.gen()}")
//        logger().info("Id Provider Name: ${idProvider.javaClass.simpleName}")
        logger().info("-------------------------------")
    }

}

@ConfigurationProperties(prefix = "set-core", ignoreUnknownFields = true)
class SETCoreConfigProperties{
    @NestedConfigurationProperty
    var appName: AppNameConfig = AppNameConfig()
    @NestedConfigurationProperty
    var idProvider : IdProviderConfig = IdProviderConfig()
}

class AppMeta : WithLogger {
    @Autowired lateinit var appNameProvider: AppNameProvider

    companion object {
        fun metaEntity(httpServletRequest : HttpServletRequest) : MetaEntity {
            val meta = MetaEntity()
            meta.url = httpServletRequest.requestURL.toString()
            meta.start = Date()
            meta.destHost = httpServletRequest.localAddr
            meta.destIp = httpServletRequest.localAddr
            meta.destPort = httpServletRequest.localPort.toString()
            meta.requestType = MetaEntity.RequestType.valueOf(httpServletRequest.method.toUpperCase())
            meta.sourceHost = httpServletRequest.remoteHost
            meta.sourceIp = httpServletRequest.remoteAddr
            meta.sourcePort = httpServletRequest.remotePort.toString()
            meta.proxyString = httpServletRequest.getParameter(MetaEntity.HEADER.PROXY_STRING_HEADER)?:""
            return meta
        }
    }
}

class IdProviderConfig{
    var type:ProviderType
    constructor(){
        this.type = ProviderType.MachineBase
    }
    constructor(typeStr:String){
        this.type = ProviderType.valueOf(typeStr)
    }
    constructor(type:ProviderType){
        this.type = type
    }
    enum class ProviderType{
        MachineBase, TimeBase, Custom
    }

}
class AppNameConfig{
    var type: BuildType
    constructor(){
        type= BuildType.None
    }
    constructor(type: BuildType){
        this.type=type
    }
    constructor(typeStr:String){
        type = BuildType.valueOf(typeStr)
    }

    enum class BuildType {
        Default, None, Custom
    }

    companion object {
        fun default(): AppNameConfig {
            val conf = AppNameConfig()
            conf.type = BuildType.Default
            return conf
        }
    }
}
