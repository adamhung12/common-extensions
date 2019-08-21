package me.xethh.libs.extension.set.core

import me.xethh.libs.extension.set.core.appProvider.AppNameProvider
import me.xethh.libs.extension.set.core.appProvider.DefaultAppNameProvider
import me.xethh.libs.extension.set.core.appProvider.NoneAppNameProvider
import me.xethh.libs.toolkits.logging.WithLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import javax.annotation.PostConstruct

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

    @Autowired lateinit var SETCoreConfigProperties: SETCoreConfigProperties
    @Autowired lateinit var appMeta: AppMeta
    @Autowired lateinit var appNameProvider: AppNameProvider

    @PostConstruct
    fun init(){
        logger().info("-------------------------------")
        logger().info("Init SET complete")
        logger().info("AppName: ${appMeta.appNameProvider.gen()}")
        logger().info("-------------------------------")
    }

}

@ConfigurationProperties(prefix = "set-core")
class SETCoreConfigProperties{
    var appName: AppNameConfig = AppNameConfig.default()
}

class AppMeta : WithLogger {
    @Autowired lateinit var appNameProvider: AppNameProvider
}

class AppNameConfig{
    lateinit var type: BuildType
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
