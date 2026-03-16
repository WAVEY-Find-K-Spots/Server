package com.Wavey.WaveyService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WaveyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaveyServiceApplication.class, args);
	}

}
