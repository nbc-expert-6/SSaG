package com.sparta.analysisservice.domain.product_analysis.infrastructure.beam;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.analysisservice.domain.product_analysis.application.service.EventEtlService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventEtlScheduler {

	private final EventEtlService eventEtlService;

	@Scheduled(cron = "0 */30 * * * *")
	public void runPeriodicEtl() {
		System.out.println("[ETL] start");
		eventEtlService.runEtlJob();
		System.out.println("[ETL] finished");
	}

}
