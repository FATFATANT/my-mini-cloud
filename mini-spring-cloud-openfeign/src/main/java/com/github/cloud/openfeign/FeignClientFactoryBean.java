package com.github.cloud.openfeign;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 生成Feign客户端的FactoryBean
 *
 * @author derek(易仁川)
 * @date 2022/4/7
 */
public class FeignClientFactoryBean implements FactoryBean<Object>, ApplicationContextAware {

    private String contextId;

    private Class<?> type;

    private ApplicationContext applicationContext;

    @Override
    public Object getObject() throws Exception {
        // 此处就和前面的Ribbon一样，每个服务对应一个容器，此处把这个容器拿出来
        FeignContext feignContext = applicationContext.getBean(FeignContext.class);
        // 我们给这个容器注册了FeignClientsConfiguration配置类，下面就是里面对应注册的若干个bean实例
        Encoder encoder = feignContext.getInstance(contextId, Encoder.class);
        Decoder decoder = feignContext.getInstance(contextId, Decoder.class);
        Contract contract = feignContext.getInstance(contextId, Contract.class);
        Client client = feignContext.getInstance(contextId, Client.class);
        // 这个东西会作为EchoService的Bean实例放入容器
        return Feign.builder()
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .client(client)
                .target(new HardCodedTarget<>(type, contextId, "http://" + contextId));
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}
