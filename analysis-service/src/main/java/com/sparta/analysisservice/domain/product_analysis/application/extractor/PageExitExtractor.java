package com.sparta.analysisservice.domain.product_analysis.application.extractor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

@Component
public class PageExitExtractor implements BaseExtractor {

	public static Extracted extractStatic(UserEventDocument event) {
		UUID sessionId = event.getSessionId();
		long ts = event.getTimestamp() != null ? event.getTimestamp().toEpochMilli() : System.currentTimeMillis();

		Extracted ex = new Extracted(event.getEventType(), null, sessionId, ts, event.getMeta());
		System.out.println("[Extractor - PageExit] " + ex);
		return ex;
	}

	@Override
	public boolean supports(String eventType) {
		return "page_exit".equalsIgnoreCase(eventType);
	}

	@Override
	public Extracted extract(UserEventDocument event) {
		return extractStatic(event);
	}

}
