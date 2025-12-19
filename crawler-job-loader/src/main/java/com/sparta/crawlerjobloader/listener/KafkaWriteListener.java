package com.sparta.crawlerjobloader.listener;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaWriteListener implements ItemWriteListener<String> {

	@Override
	public void beforeWrite(Chunk<? extends String> items) {
		log.info("[KAFKA SEND START] size={}", items.size());
	}

	@Override
	public void afterWrite(Chunk<? extends String> items) {
		log.info("[KAFKA SEND END] size={}", items.size());
	}

	@Override
	public void onWriteError(Exception e, Chunk<? extends String> items) {
		log.error("[KAFKA SEND ERROR] size={}", items.size(), e);
	}
}
