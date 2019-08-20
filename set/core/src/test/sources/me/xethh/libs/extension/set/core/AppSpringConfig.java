package me.xethh.libs.extension.set.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Base configuration for spring
 */
@Configuration
@ComponentScan("me.xethh.libs.extension.swt.core")
@EnableSETCore
public class AppSpringConfig
{
    @Autowired
    private SETCoreConfig SETCoreConfig;

    @Autowired
    private AppMeta appMeta;

    @PostConstruct
    public void init(){
        System.out.println("SST Core init successfully");
    }
}
