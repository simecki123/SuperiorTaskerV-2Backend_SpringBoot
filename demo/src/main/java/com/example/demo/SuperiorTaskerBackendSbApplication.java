package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
@Slf4j
public class SuperiorTaskerBackendSbApplication {

	@Value("${}")

	public static void main(String[] args) {
		SpringApplication.run(SuperiorTaskerBackendSbApplication.class, args);
	}

}
