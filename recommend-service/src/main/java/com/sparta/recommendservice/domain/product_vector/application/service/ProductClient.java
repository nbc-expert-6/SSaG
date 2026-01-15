package com.sparta.recommendservice.domain.product_vector.application.service;

import java.util.UUID;

import com.sparta.recommendservice.domain.product_vector.application.service.dto.ProductInfoDto;

public interface ProductClient {

	ProductInfoDto getProductInfo(UUID productId);

}
