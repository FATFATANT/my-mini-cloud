package com.github.cloud.loadbalancer.ribbon;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * 处理注解RibbonClients的配置类
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
// 这个类会被@RibbonClients给Import进来
public class RibbonClientConfigurationRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(RibbonClients.class.getName(), true);
        /*
            这个会将ServerList类型的BeanDefinition取来，spring会将其实例化，然后传给
            RibbonClientConfiguration.ribbonLoadBalancer的形参
         */
        if (attrs != null && attrs.containsKey("defaultConfiguration")) {
            String name = "default." + metadata.getClassName();  // 得到default.TutuRibbonClientConfiguration的全限定类名
            registerClientConfiguration(registry, name, attrs.get("defaultConfiguration"));
        }
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name,
                                             Object configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(RibbonClientSpecification.class);
        builder.addConstructorArgValue(name);  // 将传入的name和configuration作为构造函数的参数，即封装个一层给他注册进容器
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(name + ".RibbonClientSpecification",
                builder.getBeanDefinition());
    }
}
