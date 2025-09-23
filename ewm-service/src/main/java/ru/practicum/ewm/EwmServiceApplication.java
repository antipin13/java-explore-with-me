package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ru.practicum.ewm", "ru.practicum.ewm.client"})
public class EwmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwmServiceApplication.class, args);
    }
}
