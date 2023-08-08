package com.github.cloud.openfeign.support;

import feign.Contract;
import feign.MethodMetadata;
import feign.Request;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * feign支持Spring MVC的注解
 *
 * @author derek(易仁川)
 * @date 2022/4/9
 */
public class SpringMvcContract extends Contract.BaseContract {

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        //TODO 解析接口注解
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation annotation, Method method) {
        //解析方法注解
        //解析PostMapping注解
        if (annotation instanceof PostMapping) {
            PostMapping postMapping = (PostMapping) annotation;
            // 乍一看以为下面这句话啥也没干，但其实给restTemplate加上了请求类型
            data.template().method(Request.HttpMethod.POST);
            String path = postMapping.value()[0];
            if (!path.startsWith("/") && !data.template().path().endsWith("/")) {
                path = "/" + path;
            }
            // 注意，就是这个类把要请求的路径给找出来了
            data.template().uri(path, true);
        }

        //TODO 解析其他注解
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        //TODO 解析参数
        return true;
    }
}
