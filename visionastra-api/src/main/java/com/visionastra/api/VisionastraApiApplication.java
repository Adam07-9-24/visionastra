package com.visionastra.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VisionastraApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(VisionastraApiApplication.class, args);
	}
}
