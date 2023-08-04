package com.github.cloud.examples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author derek(易仁川)
 * @date 2022/3/19
 */
@RestController
@SpringBootApplication
public class ProviderApplication {

    @Value("${server.port}")
    private Integer port;

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);  // 这个方法其实有返回值，不过大部分情况下也不会接收
    }

    @PostMapping("/echo")
    public String echo() {
        return "Port of the service provider: " + port;
    }
}
