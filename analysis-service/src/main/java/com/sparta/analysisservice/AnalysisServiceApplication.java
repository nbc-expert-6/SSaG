package com.sparta.analysisservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalysisServiceApplication.class, args);
	}

}
