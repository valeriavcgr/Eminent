package com.example.Eminent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación Spring Boot Eminent.
 * Habilita el autoconfigurado de Spring Boot y el soporte para tareas programadas
 * (scheduler de cierre de eventos).
 */
@SpringBootApplication
@EnableScheduling
public class EminentApplication {
	public static void main(String[] args) {
		SpringApplication.run(EminentApplication.class, args);
	}
}