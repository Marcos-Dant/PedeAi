package com.PedeAi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PedeAiApplication {

    public static void main(String[] args) {
        System.setProperty("spring.datasource.username", "root");
        System.setProperty("spring.datasource.password", "root");

        SpringApplication.run(PedeAiApplication.class, args);
    }

}
