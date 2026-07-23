package com.pix.recorrente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PixRecorrenteApplication {
    public static void main(String[] args) {
        SpringApplication.run(PixRecorrenteApplication.class, args);
    }
}
