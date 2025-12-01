package com.sparta.recommendservice.domain.product_vector.application.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendCacheService {

	private final StringRedisTemplate redisTemplate;

	private static final String KEY_PREFIX = "recommend:";
	private static final long TTL_HOURS = 24;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public void saveRecommend(UUID productId, List<UUID> recommendedIds) {
		String key = KEY_PREFIX + productId;
		try {
			String value = objectMapper.writeValueAsString(recommendedIds); // JSON 직렬화
			redisTemplate.opsForValue().set(key, value, Duration.ofHours(TTL_HOURS));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Redis save serialization error", e);
		}
	}

	public List<UUID> getRecommend(UUID productId) {
		String key = KEY_PREFIX + productId;
		String value = redisTemplate.opsForValue().get(key);

		if (value == null) {
			return Collections.emptyList();
		}

		try {
			return objectMapper.readValue(value, new TypeReference<List<UUID>>() {
			});
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Redis read deserialization error", e);
		}
	}

}