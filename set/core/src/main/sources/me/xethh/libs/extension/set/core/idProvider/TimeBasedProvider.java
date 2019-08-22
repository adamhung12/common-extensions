package me.xethh.libs.extension.set.core.idProvider;

import me.xethh.utils.dateManipulation.DateFactory;
import me.xethh.utils.dateManipulation.DateFormatBuilder;

import java.util.concurrent.atomic.AtomicLong;

public class TimeBasedProvider implements IdProvider {
    AtomicLong longProvider = new AtomicLong(0);
    @Override
    public String gen() {
        String timestamp = DateFactory.now().format(DateFormatBuilder.Format.NUMBER_DATETIME);
        return timestamp+"_"+ String.format("%09d", longProvider.incrementAndGet());
    }
}
