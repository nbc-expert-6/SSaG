package com.sparta.analysisservice.domain.product_analysis.application.service;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public interface PythonRunner {
	void run() throws InterruptedException, IOException;
}
