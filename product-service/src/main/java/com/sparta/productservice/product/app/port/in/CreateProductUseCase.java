package com.sparta.productservice.product.app.port.in;

import com.sparta.productservice.product.app.command.CreateProductCommand;

public interface CreateProductUseCase {
	void createProduct(CreateProductCommand command);
}