package com.irrigo.farmservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // CRUCIAL : C'est cette annotation qui crée le bean pour TaskServiceClient
public class FarmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FarmServiceApplication.class, args);
    }
}