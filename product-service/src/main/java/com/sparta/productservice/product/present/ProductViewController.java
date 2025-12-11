package com.sparta.productservice.product.present;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductViewController {

	@GetMapping("/products")
	public String productList() {
		return "product-list";  // templates/product-list.html
	}

	@GetMapping("/products/{id}")
	public String productDetail(@PathVariable UUID id) {
		return "product-detail";  // templates/product-detail.html
	}
}
