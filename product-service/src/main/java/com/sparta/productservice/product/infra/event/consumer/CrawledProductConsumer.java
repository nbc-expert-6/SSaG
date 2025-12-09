package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.sparta.productservice.product.infra.event.handler.CrawledProductHandler;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledProductConsumer {

	private final CrawledProductHandler handler;

	@KafkaListener(
		topics = "product-details",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "crawledProductKafkaListenerContainerFactory"
	)
	public void consume(CrawledProductMessage message, Acknowledgment ack) {
		log.info(message.toString());
		log.info("Consumed Product: mainProductId={}", message.mainProductId());

		try {
			handler.handleCrawledProduct(message);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("Failed to process: {}", e.getMessage(), e);
		}
	}
}
