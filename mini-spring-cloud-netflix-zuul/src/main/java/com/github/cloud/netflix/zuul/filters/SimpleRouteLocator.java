package com.github.cloud.netflix.zuul.filters;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Map;

/**
 * 路由定位器实现类，这个类只有PreDecorationFilter会用到
 *
 * @author derek(易仁川)
 * @date 2022/6/28
 */
public class SimpleRouteLocator implements RouteLocator {

	private ZuulProperties zuulProperties;
	// 根据这个匹配起就可以对路径进行解析
	private PathMatcher pathMatcher = new AntPathMatcher();

	public SimpleRouteLocator(ZuulProperties zuulProperties) {
		this.zuulProperties = zuulProperties;
	}

	@Override
	public Route getMatchingRoute(String path) {  // 找到就返回说明写在越前面的越早匹配，或者说写在后面的同匹配规则路由永远不会被匹配
		for (Map.Entry<String, ZuulProperties.ZuulRoute> entry : zuulProperties.getRoutes().entrySet()) {
			ZuulProperties.ZuulRoute zuulRoute = entry.getValue();
			String pattern = zuulRoute.getPath();
			if (pathMatcher.match(pattern, path)) {
				String targetPath = path.substring(pattern.indexOf("*") - 1);  // 去掉服务名，仅保留postMapping上的注解
				return new Route(targetPath, zuulRoute.getServiceId());
			}
		}

		return null;
	}
}