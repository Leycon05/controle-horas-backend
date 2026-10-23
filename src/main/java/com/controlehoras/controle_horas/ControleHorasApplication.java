package com.controlehoras.controle_horas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.controlehoras.entity")
@EnableJpaRepositories(basePackages = "com.controlehoras.repository")
public class ControleHorasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControleHorasApplication.class, args);
    }
}