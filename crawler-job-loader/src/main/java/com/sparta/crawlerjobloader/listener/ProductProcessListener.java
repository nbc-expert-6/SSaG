package com.sparta.crawlerjobloader.listener;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.stereotype.Component;

import com.sparta.crawlerjobloader.domain.entity.MainProduct;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProductProcessListener implements ItemProcessListener<MainProduct, String> {

	private int count = 0;

	@Override
	public void beforeProcess(MainProduct item) {}

	@Override
	public void afterProcess(MainProduct item, String result) {
		count++;
	}

	@Override
	public void onProcessError(MainProduct item, Exception e) {}

	@AfterChunk
	public void afterChunk() {
		log.info("[PROCESS DONE] processedCount={}", count);
		count = 0;
	}
}
