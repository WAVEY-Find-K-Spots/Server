package com.Wavey.WaveyService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;

@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties(MediaCollectProperties.class)
@SpringBootApplication
public class WaveyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaveyServiceApplication.class, args);
	}

}
