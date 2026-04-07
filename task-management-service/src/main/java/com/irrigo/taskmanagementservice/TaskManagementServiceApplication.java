package com.irrigo.taskmanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Indispensable pour activer les tâches planifiées
public class TaskManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementServiceApplication.class, args);
    }
}