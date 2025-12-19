package com.sparta.crawlerjobloader.batch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.batch.domain.entity.MainProduct;

@Component
public class ProductProcessor implements ItemProcessor<MainProduct, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String process(MainProduct product) throws Exception {
		Map<String, Object> payload = new HashMap<>();
		payload.put("main_product_id", product.getId());
		payload.put("name", product.getName());
		return objectMapper.writeValueAsString(payload);
	}
}