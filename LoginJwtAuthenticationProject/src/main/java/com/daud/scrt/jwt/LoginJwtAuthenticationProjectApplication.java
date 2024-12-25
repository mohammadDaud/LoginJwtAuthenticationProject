package com.daud.scrt.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.daud.scrt.jwt")
public class LoginJwtAuthenticationProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(LoginJwtAuthenticationProjectApplication.class, args);
	}

}
