package com.github.cloud.loadbalancer.ribbon.config;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.loadbalancer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.netflix.client.config.IClientConfig;

/**
 * 配置ribbon默认组件
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
@Configuration
public class RibbonClientConfiguration {

    @Value("${ribbon.client.name}")
    private String name;

    @Bean
    @ConditionalOnMissingBean
    public IClientConfig ribbonClientConfig() {
        // 定义加载和读取ribbon客户端配置的方法
        DefaultClientConfigImpl config = new DefaultClientConfigImpl();
        config.loadProperties(name);
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public IRule ribbonRule(IClientConfig config) {
        // 根据所属zone和可用性筛选服务实例，在没有多zone的情况下退化为轮询RoundRobinRule
        ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing(IClientConfig config) {
        // 不做检查，认为服务存活
        return new DummyPing();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<Server> ribbonServerList(IClientConfig config) {
        // 这个方法由于有@ConditionalOnMissingBean这个注解，所以这个方法应该有不执行的可能
        ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerListUpdater ribbonServerListUpdater(IClientConfig config) {
        return new PollingServerListUpdater(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerListFilter<Server> ribbonServerListFilter(IClientConfig config) {
        ServerListSubsetFilter filter = new ServerListSubsetFilter();
        filter.initWithNiwsConfig(config);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
                                            ServerList<Server> serverList, ServerListFilter<Server> serverListFilter,
                                            IRule rule, IPing ping, ServerListUpdater serverListUpdater) {
        return new ZoneAwareLoadBalancer<>(config, rule, ping, serverList,
                serverListFilter, serverListUpdater);
    }
}






























