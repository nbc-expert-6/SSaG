package com.sparta.analysisservice.domain.product_analysis.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyAnalysisScheduler {

	private final PythonRunner pythonRunner;

	@Scheduled(cron = "0 0 23 * * *")
	public void runDailyAnalysis() {
		log.info("[BATCH START] Daily user behavior analysis");

		try {
			pythonRunner.run();
			log.info("[BATCH SUCCESS] Daily analysis completed");
		} catch (Exception e) {
			log.error("[BATCH FAIL] Daily analysis failed", e);
		}
	}

}
