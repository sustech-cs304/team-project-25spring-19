package com.example.cs304project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;


@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
public class Cs304ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(Cs304ProjectApplication.class, args);
	}

}
