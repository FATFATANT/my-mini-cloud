package com.github.cloud.netflix.zuul;

import java.util.Map;

import com.github.cloud.netflix.zuul.filters.RouteLocator;
import com.github.cloud.netflix.zuul.filters.SimpleRouteLocator;
import com.github.cloud.netflix.zuul.filters.ZuulProperties;
import com.github.cloud.netflix.zuul.filters.post.SendResponseFilter;
import com.github.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import com.github.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import com.github.cloud.netflix.zuul.metrics.EmptyCounterFactory;
import com.github.cloud.netflix.zuul.metrics.EmptyTracerFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.http.ZuulServlet;
import com.netflix.zuul.monitoring.CounterFactory;
import com.netflix.zuul.monitoring.TracerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zuul API网关自动配置类
 *
 * @author derek(易仁川)
 * @date 2022/6/23
 */
@Configuration
@EnableConfigurationProperties({ZuulProperties.class})
public class ZuulServerAutoConfiguration {

	@Autowired
	protected ZuulProperties zuulProperties;

	/**
	 * 注册ZuulServlet，用于拦截处理http请求
	 */
	@Bean
	public ServletRegistrationBean zuulServlet() {
		// 下面这个是我们根据拦截的根路径生成Servlet，所以理论上应该网关可以有多个servlet
		// SpringBoot中如果想要向容器中加入Servlet，就必须把这个包进ServletRegistrationBean
		return new ServletRegistrationBean<>(new ZuulServlet(), zuulProperties.getServletPath());
	}

	/**
	 * 路由定位器
	 */
	@Bean
	public RouteLocator simpleRouteLocator() {
		return new SimpleRouteLocator(zuulProperties);
	}

	/**
	 * pre类型过滤器，根据RouteLocator来进行路由规则的匹配
	 */
	@Bean
	public ZuulFilter preDecorationFilter(RouteLocator routeLocator) {
		return new PreDecorationFilter(routeLocator);
	}

	/**
	 * route类型过滤器，使用ribbon负载均衡器进行http请求
	 */
	@Bean
	ZuulFilter ribbonRoutingFilter(LoadBalancerClient loadBalancerClient) {
		return new RibbonRoutingFilter(loadBalancerClient);
	}

	/**
	 * post类型过滤器，向客户端输出响应报文
	 */
	@Bean
	ZuulFilter sendResponseFilter() {
		return new SendResponseFilter();
	}

	/**
	 * 注册过滤器
	 * 此处应该是Spring会将所有的ZuulFilter实现类给扔进来
	 */
	@Bean
	public FilterRegistry filterRegistry(Map<String, ZuulFilter> filterMap) {
		/*
			我大概懂了，就是这个返回值本身没啥用，因为放到容器中也没别人用
			这里最关键的是第一句，因为它都写成了单例，这个是取到单例的对象然后直接在那个对象上注册所有的过滤器
			spring在此处的用处就是能够将前面写好的ZuulFilter自动通过形参传入
		 */
		FilterRegistry filterRegistry = FilterRegistry.instance();
		filterMap.forEach((name, filter) -> {
			filterRegistry.put(name, filter);
		});
		return filterRegistry;
	}

	//监控相关类，不必关注-------------------------------

	@Bean
	public CounterFactory emptyCounterFactory() {
		CounterFactory.initialize(new EmptyCounterFactory());
		return CounterFactory.instance();
	}

	@Bean
	public TracerFactory emptyTracerFactory() {
		TracerFactory.initialize(new EmptyTracerFactory());
		return TracerFactory.instance();
	}
}
