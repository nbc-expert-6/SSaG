package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.common.kafka.consumer.AbstractKafkaConsumer;
import com.sparta.productservice.product.infra.event.handler.CrawledProductHandler;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledProductConsumer extends AbstractKafkaConsumer<CrawledProductMessage> {

	private final CrawledProductHandler handler;

	@KafkaListener(
		topics = KafkaTopicType.Topics.PRODUCT_DETAILS,
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(CrawledProductMessage message) {
		log.info("📥 Consumed Product: mainProductId={}, url={}",
			message.mainProductId(), message.saleLink());

		handler.handleCrawledProduct(message);

		log.info("✅ Successfully processed product: mainProductId={}",
			message.mainProductId());
	}
}
