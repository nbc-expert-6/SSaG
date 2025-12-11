package com.sparta.analysisservice.domain.product_analysis.application.transformer;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.apache.beam.sdk.transforms.DoFn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonToUserEventFn extends DoFn<String, UserEventDocument> {

	private final ObjectMapper mapper = new ObjectMapper();

	@ProcessElement
	public void process(ProcessContext context) {
		String json = context.element();
		try {
			Map<String, Object> m = mapper.readValue(json, Map.class);

			UserEventDocument e = UserEventDocument.builder()
				.sessionId(m.get("sessionId") != null ? UUID.fromString(m.get("sessionId").toString()) : null)
				.productId(m.get("productId") != null ? UUID.fromString(m.get("productId").toString()) : null)
				.eventType(m.get("eventType") != null ? m.get("eventType").toString() : null)
				.timestamp(m.get("timestamp") != null ? Instant.parse(m.get("timestamp").toString()) : Instant.now())
				.meta(m.get("meta") != null ? mapper.writeValueAsString(m.get("meta")) : "{}")
				.kafkaPublished(false)
				.build();

			context.output(e);

		} catch (Exception ex) {
			log.error("JsonToUserEventFn parse error: " + ex.getMessage());
		}
	}

}
