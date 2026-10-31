package com.resumelens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResumelensApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumelensApplication.class, args);
    }
}