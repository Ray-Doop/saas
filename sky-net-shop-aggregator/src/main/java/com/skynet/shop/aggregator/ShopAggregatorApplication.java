package com.skynet.shop.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ShopAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopAggregatorApplication.class, args);
    }
}
