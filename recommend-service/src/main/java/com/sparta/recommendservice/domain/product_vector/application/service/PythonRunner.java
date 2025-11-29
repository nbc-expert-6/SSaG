package com.sparta.recommendservice.domain.product_vector.application.service;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public interface PythonRunner {
	void run() throws InterruptedException, IOException;
}
