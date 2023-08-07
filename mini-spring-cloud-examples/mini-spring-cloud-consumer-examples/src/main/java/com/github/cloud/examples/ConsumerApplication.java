package com.github.cloud.examples;

import com.github.cloud.tutu.discovery.TutuDiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

/**
 * @author derek(易仁川)
 * @date 2022/3/20
 */
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Configuration
    static class RestTemplateConfiguration {

        /**
         * 赋予负载均衡的能力
         * 这个是已经封装好的用法，就是给restTemplate加上一个@LoadBalanced，它就具备了负载均衡的能力
         * @return
         */
        @LoadBalanced
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @RestController
    static class HelloController {

        @Autowired
        private TutuDiscoveryClient discoveryClient;

        @Autowired
        private LoadBalancerClient loadBalancerClient;

        @Autowired
        private RestTemplate loadBalancedRestTemplate;
        // 注意，下面这个restTemplate是自己new的，所以不被容器接管，也没有负载均衡能力，我们此时通过自己的逻辑实现
        private RestTemplate restTemplate = new RestTemplate();

        @GetMapping("/hello")
        public String hello() {
            // 这里就可以作一个对比，服务发现客户端也可以得到一个服务对应的一系列实例，但是这里并不做负载均衡，而是取第一个
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances("provider-application");
            if (serviceInstances.size() > 0) {
                ServiceInstance serviceInstance = serviceInstances.get(0);
                URI uri = serviceInstance.getUri();
                String response = restTemplate.postForObject(uri.toString() + "/echo", null, String.class);
                return response;
            }

            throw new RuntimeException("No service instance for provider-application found");
        }

        @GetMapping("/world")
        public String world() {
            // 传入服务名给负载均衡器，让其返回一个实例，获取实例的uri然后向其发送请求
            ServiceInstance serviceInstance = loadBalancerClient.choose("provider-application");
            if (serviceInstance != null) {
                URI uri = serviceInstance.getUri();
                String response = restTemplate.postForObject(uri.toString() + "/echo", null, String.class);
                return response;
            }

            throw new RuntimeException("No service instance for provider-application found");
        }

        @GetMapping("/foo")
        public String foo() {
            // 简化服务调用版本
            return loadBalancedRestTemplate.postForObject("http://provider-application/echo", null, String.class);
        }
    }
}

