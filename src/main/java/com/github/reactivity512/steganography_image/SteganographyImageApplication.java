package com.github.reactivity512.steganography_image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SteganographyImageApplication {

	public static void main(String[] args) {
		SpringApplication.run(SteganographyImageApplication.class, args);
	}
}
