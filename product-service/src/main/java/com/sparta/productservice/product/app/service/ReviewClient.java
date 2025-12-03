package com.sparta.productservice.product.app.service;

import java.util.List;
import java.util.UUID;

import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.app.service.dto.ReviewInfo;

public interface ReviewClient {
	List<ReviewInfo> getReviewsByMainProductId(UUID mainProductId, PageSizeType pageSize);
}
