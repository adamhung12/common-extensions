package me.xethh.libs.extension.set.sst.core.FirstLayerFilter.requestResponsePreprocess;

import me.xethh.libs.toolkits.aspectInterface.Aspect;
import me.xethh.libs.toolkits.webDto.core.MetaEntity;
import me.xethh.libs.toolkits.webDto.core.request.Request;
import me.xethh.libs.toolkits.webDto.core.response.Response;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public abstract class WebDtosAspect extends Aspect {
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Override
    public Object executeTask(ProceedingJoinPoint joinPoint) throws Throwable {
        if(joinPoint.getSignature() instanceof MethodSignature){
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            Class[] paramTypes = signature.getParameterTypes();
            MetaEntity meta = metaSetup();
            for(int i=0;i<paramTypes.length;i++){
                if(Request.class.isAssignableFrom(paramTypes[i]) && args[i]!=null){
                    ((Request)args[i]).setId(MDC.get(MetaEntity.HEADER.REQUEST_ID_HEADER));
                    ((Request)args[i]).setMeta(meta);
                }
                if(Response.class.isAssignableFrom(paramTypes[i])){
                    if(args[i]==null){
                        Response response = (Response) paramTypes[i].getConstructor().newInstance();
                        response.setId(MDC.get(MetaEntity.HEADER.REQUEST_ID_HEADER));
                    }
                    else {
                        ((Response) args[i]).setId(MDC.get(MetaEntity.HEADER.REQUEST_ID_HEADER));
                    }
                    ((Response) args[i]).setMeta(meta);
                }
            }
        }
        Object obj = joinPoint.proceed();
        if(obj!=null && obj instanceof Response)
            ((Response) obj).getMeta().setEnd(new Date());
        return obj;
    }

    public MetaEntity metaSetup(){
        MetaEntity meta = new MetaEntity();
        meta.setUrl(httpServletRequest.getRequestURL().toString());
        meta.setStart(new Date());
        meta.setDestHost(httpServletRequest.getLocalAddr());
        meta.setDestIp(httpServletRequest.getLocalAddr());
        meta.setDestPort(httpServletRequest.getLocalPort()+"");
        meta.setRequestType(MetaEntity.RequestType.valueOf(httpServletRequest.getMethod().toUpperCase()));
        meta.setSourceHost(httpServletRequest.getRemoteHost());
        meta.setSourceIp(httpServletRequest.getRemoteAddr());
        meta.setSourcePort(httpServletRequest.getRemotePort()+"");
        meta.setProxyString(MDC.get(MetaEntity.HEADER.PROXY_STRING_HEADER));
        return meta;
    }

}
