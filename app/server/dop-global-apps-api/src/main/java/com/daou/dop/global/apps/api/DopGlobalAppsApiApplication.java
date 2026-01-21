package com.daou.dop.global.apps.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "com.daou.dop")
@EntityScan(basePackages = "com.daou.dop")
public class DopGlobalAppsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DopGlobalAppsApiApplication.class, args);
    }
}
