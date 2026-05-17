package com.skynet.product.workshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductWorkshopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductWorkshopApplication.class, args);
    }
}
