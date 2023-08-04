package com.github.cloud.examples;

import com.github.cloud.tutu.discovery.TutuDiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
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

    @RestController
    static class HelloController {

        @Autowired
        private TutuDiscoveryClient discoveryClient;

        private RestTemplate restTemplate = new RestTemplate();

        @GetMapping("/hello")
        public String hello() {
            /*
                此处的逻辑非常简化，就是向tutuServer查所有指定的服务实例，然后取查到的列表中的第一项，取出其url然后发起http请求
             */
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances("provider-application");
            if (serviceInstances.size() > 0) {
                ServiceInstance serviceInstance = serviceInstances.get(0);
                URI uri = serviceInstance.getUri();
                String response = restTemplate.postForObject(uri.toString() + "/echo", null, String.class);
                return response;
            }

            throw new RuntimeException("No service instance for provider-application found");
        }
    }
}

