package com.bookstore.entity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bookstore.entity", "com.example.common"})
public class EntityServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EntityServiceApplication.class, args);
    }
}
