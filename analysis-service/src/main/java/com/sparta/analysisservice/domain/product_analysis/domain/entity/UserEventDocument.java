package com.sparta.analysisservice.domain.product_analysis.domain.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Document(indexName = "user-events")
@EqualsAndHashCode
public class UserEventDocument implements Serializable {

	@Id
	private String id;

	private UUID sessionId;

	private UUID productId;

	private String eventType;

	@Field(type = FieldType.Date, format = {DateFormat.date_time})
	private Instant timestamp;

	private String meta;

	@Field(type = FieldType.Boolean)
	private boolean kafkaPublished;

	public void setKafkaPublished(boolean kafkaPublished) {
		this.kafkaPublished = kafkaPublished;
	}

	@Builder
	public UserEventDocument(UUID sessionId, UUID productId, String eventType,
		Instant timestamp, String meta, boolean kafkaPublished) {
		this.sessionId = sessionId;
		this.productId = productId;
		this.eventType = eventType;
		this.timestamp = timestamp;
		this.meta = meta;
		this.kafkaPublished = kafkaPublished;
	}

}
