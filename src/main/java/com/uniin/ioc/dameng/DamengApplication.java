package com.uniin.ioc.dameng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class DamengApplication {

    public static void main(String[] args) {
        SpringApplication.run(DamengApplication.class, args);
    }

}
