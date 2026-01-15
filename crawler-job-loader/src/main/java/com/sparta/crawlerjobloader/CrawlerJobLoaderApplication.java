package com.sparta.crawlerjobloader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrawlerJobLoaderApplication implements CommandLineRunner {
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job productJob;

	public static void main(String[] args) {
		SpringApplication.run(CrawlerJobLoaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// JobParameters: 같은 Job을 여러 번 실행할 때 구분용
		JobParameters params = new JobParametersBuilder()
			.addLong("time", System.currentTimeMillis())
			.toJobParameters();

		jobLauncher.run(productJob, params);
	}
}
