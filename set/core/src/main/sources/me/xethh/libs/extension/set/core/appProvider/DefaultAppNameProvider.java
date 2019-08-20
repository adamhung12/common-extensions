package me.xethh.libs.extension.set.core.appProvider;

import java.lang.management.ManagementFactory;

public class DefaultAppNameProvider implements AppNameProvider{
    String appInfo = String.format(ManagementFactory.getRuntimeMXBean().getName());

    @Override
    public String gen() {
        return appInfo;
    }
}
