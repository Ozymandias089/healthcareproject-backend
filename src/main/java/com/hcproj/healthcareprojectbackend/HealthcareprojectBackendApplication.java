package com.hcproj.healthcareprojectbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HealthcareprojectBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthcareprojectBackendApplication.class, args);
	}

}
