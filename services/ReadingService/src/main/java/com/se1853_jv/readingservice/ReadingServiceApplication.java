package com.se1853_jv.readingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ReadingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadingServiceApplication.class, args);
	}

}
