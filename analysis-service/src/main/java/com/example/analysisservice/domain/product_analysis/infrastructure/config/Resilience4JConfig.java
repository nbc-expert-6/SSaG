package com.example.analysisservice.domain.product_analysis.infrastructure.config;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class Resilience4JConfig {

	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
			// 실패 비율이상이면 CircuitBreaker를 Open 상태로 전환
			.failureRateThreshold(50)
			// Open상태일때 다시 Half-Open 상태로 넘어가기 전 대기 시간
			.waitDurationInOpenState(Duration.ofSeconds(30))
			// 호출 횟수 기준으로 실패율 계산
			.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
			// 최근 몇번 호출을 기준으로 실패율 계산할지
			.slidingWindowSize(10)
			.build();

		// 메소드 실행 시간이 너무 오래 걸리면 실패로 처리함
		TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
			.timeoutDuration(Duration.ofSeconds(5))
			.build();

		return factory -> factory.configureDefault(
			id -> new Resilience4JConfigBuilder(id)
				.timeLimiterConfig(timeLimiterConfig)
				.circuitBreakerConfig(circuitBreakerConfig)
				.build()
		);
	}

}
