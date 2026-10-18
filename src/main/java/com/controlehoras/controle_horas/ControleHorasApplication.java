package com.controlehoras.controle_horas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.controlehoras")
@EnableJpaRepositories(basePackages = "com.controlehoras.repository")
@EntityScan(basePackages = "com.controlehoras.model")
public class ControleHorasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControleHorasApplication.class, args);
    }
}