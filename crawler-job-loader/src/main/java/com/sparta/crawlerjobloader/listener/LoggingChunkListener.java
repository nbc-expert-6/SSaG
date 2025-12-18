package com.sparta.crawlerjobloader.listener;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoggingChunkListener implements ChunkListener {

	@Override
	public void beforeChunk(ChunkContext context) {
		log.info("[CHUNK START]");
	}

	@Override
	public void afterChunk(ChunkContext context) {
		log.info("[CHUNK END]");
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		log.error("[CHUNK ERROR]");
	}
}
