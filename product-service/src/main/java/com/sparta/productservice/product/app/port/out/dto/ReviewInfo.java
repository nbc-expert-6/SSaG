package com.sparta.productservice.product.app.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.product.domain.vo.PlatformType;

public record ReviewInfo(
	UUID reviewId,
	BigDecimal rating,
	PlatformType platformType,
	String authorName,
	String title,
	String content,
	List<String> images,
	LocalDateTime createdAt
) {
}
