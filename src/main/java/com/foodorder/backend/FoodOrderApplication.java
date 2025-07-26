package com.foodorder.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FoodOrderApplication {

	@PostConstruct
	public void printProfile() {
	}

	public static void main(String[] args) {
		SpringApplication.run(FoodOrderApplication.class, args);

	}

}
