package com.foodorder.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FoodOrderApplication {
	@Value("${spring.profiles.active:default}")
	private String profile;

	@PostConstruct
	public void printProfile() {
		System.out.println(">>> Profile đang chạy: " + profile);
	}

	public static void main(String[] args) {
		SpringApplication.run(FoodOrderApplication.class, args);

	}

}
