package com.example.analysisservice.domain.product_analysis.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteController {

	@GetMapping("/test")
	public String test() {
		return "click";
	}

}
