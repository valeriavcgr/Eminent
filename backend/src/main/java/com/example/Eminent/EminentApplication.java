package com.example.Eminent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EminentApplication {
	public static void main(String[] args) {
		SpringApplication.run(EminentApplication.class, args);
	}
}