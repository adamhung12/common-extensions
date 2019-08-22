package me.xethh.libs.extension.set.core.idProvider;


import me.xethh.utils.dateManipulation.DateFormatBuilderImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class MachineBasedProvider implements IdProvider {
    SimpleDateFormat df = DateFormatBuilderImpl.get().year4Digit().month2Digit().day2Digit().hourInDay24().minute().build();

    private String serviceId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    private AtomicLong longProvider = new AtomicLong(0);

    private String curTimeString = getTime();
    private String getTime(){
        String timeString = df.format(new Date());
        if(timeString!=null && !timeString.equals(curTimeString)){
            curTimeString = timeString;
            longProvider.set(0);
        }
        return curTimeString;
    }
    @Override
    public String gen() {
        StringBuilder builder = new StringBuilder();
        builder.append(serviceId).append(getTime()).append(String.format("%08d",longProvider.incrementAndGet()));
        return builder.toString();
    }
}
