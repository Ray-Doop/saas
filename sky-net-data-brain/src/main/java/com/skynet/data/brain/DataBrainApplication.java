package com.skynet.data.brain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DataBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataBrainApplication.class, args);
    }
}
