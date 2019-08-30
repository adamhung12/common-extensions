package me.xethh.libs.extension.set.sst.core.FirstLayerFilter.requestResponsePreprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.xethh.libs.extension.set.core.RequestMeta;
import me.xethh.libs.toolkits.aspectInterface.Aspect;
import me.xethh.libs.toolkits.webDto.core.MetaEntity;
import me.xethh.libs.toolkits.webDto.core.WebBaseEntity;
import me.xethh.libs.toolkits.webDto.core.request.Request;
import me.xethh.libs.toolkits.webDto.core.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.Date;

public abstract class WebDtosAspect extends Aspect {
    @Autowired
    private HttpServletRequest httpServletRequest;
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RequestMeta requestMeta;

    @Override
    public Object executeTask(ProceedingJoinPoint joinPoint) throws Throwable {
        Object processResult = null;
        if(joinPoint.getSignature() instanceof MethodSignature){
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            Class[] paramTypes = signature.getParameterTypes();
            Annotation[][] annotationTypes = signature.getMethod().getParameterAnnotations();

            MetaEntity meta = requestMeta.getMetaEntity();

            for(int i=0;i<paramTypes.length;i++){
                if("get".equalsIgnoreCase(httpServletRequest.getMethod()) && annotations(i, GetRequestBody.class, annotationTypes)!=null){
                    GetRequestBody annotation = annotations(i, GetRequestBody.class, annotationTypes);
                    if(StringUtils.isNotEmpty(httpServletRequest.getParameter(annotation.value()))){
                        String requestString = httpServletRequest.getParameter(annotation.value());
                        Object obj = objectMapper.readValue(requestString, paramTypes[i]);
                        args[i] = obj;
                    }
                }
                if(args[i]!=null){
                    if(Request.class.isAssignableFrom(paramTypes[i])){
                        ((Request)args[i]).setId(requestMeta.getId());
                    }
                    if(Response.class.isAssignableFrom(paramTypes[i])){
                        if(args[i]==null)
                            args[i] = paramTypes[i].getConstructor().newInstance();
                        ((Response) args[i]).setId(requestMeta.getId());
                    }
                    if(WebBaseEntity.class.isAssignableFrom(paramTypes[i]))
                        ((WebBaseEntity) args[i]).setMeta(meta);
                }
            }
            processResult = joinPoint.proceed(args);
        }
        else
            processResult = joinPoint.proceed();

        if(processResult!=null
                && processResult instanceof Response
                && ((Response) processResult).getMeta()!=null
        )
            ((Response) processResult).getMeta().setEnd(new Date());
        return processResult;
    }

    public <A extends Annotation> A annotations(Integer index, Class<A> clazz, Annotation[][] annotations){
        for(int i=0; i<annotations[index].length;i++){
            if(clazz.isAssignableFrom(annotations[index][i].getClass())){
                return (A)annotations[index][i];
            }
        }
        return null;
    }


}
