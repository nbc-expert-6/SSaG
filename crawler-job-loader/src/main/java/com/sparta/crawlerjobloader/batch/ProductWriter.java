package com.sparta.crawlerjobloader.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductWriter implements ItemWriter<String> {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public ProductWriter(
		KafkaTemplate<String, String> kafkaTemplate
	) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void write(Chunk<? extends String> chunk) {
		for (String msg : chunk) {
			kafkaTemplate.send("keywords", msg);
		}
	}
}