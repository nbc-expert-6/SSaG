package com.sparta.crawlerjobloader.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.sparta.crawlerjobloader.batch.ProductProcessor;
import com.sparta.crawlerjobloader.batch.ProductReader;
import com.sparta.crawlerjobloader.batch.ProductWriter;
import com.sparta.crawlerjobloader.domain.entity.MainProduct;
import com.sparta.crawlerjobloader.listener.KafkaWriteListener;
import com.sparta.crawlerjobloader.listener.LoggingChunkListener;
import com.sparta.crawlerjobloader.listener.ProductProcessListener;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	private final ProductReader reader;
	private final ProductProcessor processor;
	private final ProductWriter writer;

	public BatchConfig(ProductReader reader,
		ProductProcessor processor,
		ProductWriter writer) {
		this.reader = reader;
		this.processor = processor;
		this.writer = writer;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	@Bean
	public Step productStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		ProductProcessListener productProcessListener,
		KafkaWriteListener kafkaWriteListener,
		LoggingChunkListener loggingChunkListener
	) {
		return new StepBuilder("publishCrawlerTaskStep", jobRepository)
			.<MainProduct, String>chunk(100, transactionManager)
			.reader(reader.productReader())
			.processor(processor)
			.writer(writer)
			.listener(productProcessListener)
			.listener(kafkaWriteListener)
			.listener(loggingChunkListener)
			.allowStartIfComplete(true)
			.build();
	}

	@Bean
	public Job productJob(JobRepository jobRepository, Step productStep) {
		return new JobBuilder("publishCrawlerTaskJob", jobRepository)
			.start(productStep)
			.build();
	}
}