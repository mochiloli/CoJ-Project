package com.mfec.speech.Recognize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com")
public class RecognizeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecognizeApplication.class, args);
	}

}
