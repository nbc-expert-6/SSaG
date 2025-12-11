package com.sparta.analysisservice.domain.product_analysis.infrastructure.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sparta.analysisservice.domain.product_analysis.application.extractor.BaseExtractor;
import com.sparta.analysisservice.domain.product_analysis.application.transformer.JsonToUserEventFn;
import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EtlPipeline {

	private final PipelineOptions options;

	@Value("${etl.elasticsearch.host}")
	private String esHost;

	@Value("${etl.elasticsearch.index}")
	private String esIndex;

	public Pipeline create() {
		return Pipeline.create(options);
	}

	// ES에서 읽어 UserEvent PCollection 반환
	public PCollection<UserEventDocument> readFromElastic(Pipeline p) {
		ElasticsearchIO.ConnectionConfiguration connectionConfiguration =
			ElasticsearchIO.ConnectionConfiguration.create(
				new String[] {esHost},
				esIndex
			);

		PCollection<String> jsons = p.apply("ReadFromEs",
			ElasticsearchIO.read()
				.withConnectionConfiguration(connectionConfiguration)
		);

		return jsons
			.apply("JsonToUserEvent", ParDo.of(new JsonToUserEventFn()))
			.setCoder(SerializableCoder.of(UserEventDocument.class));
	}

	// Extracted -> DataLake(간단 JSON lines) 저장
	public void writeExtractedToDataLake(PCollection<BaseExtractor.Extracted> extracted,
		String pathPrefix) {
		extracted.apply("ExtractedToJSON", MapElements.into(TypeDescriptors.strings())
				.via(e -> {
					ObjectMapper mapper = new ObjectMapper();
					try {
						// metaJson 문자열 -> JsonNode 변환
						JsonNode metaNode = mapper.readTree(e.getMetaJson());

						// 새 ObjectNode 생성
						ObjectNode output = mapper.createObjectNode();
						output.put("eventType", e.getEventType());
						output.put("productId", e.getProductId() != null ? e.getProductId().toString() : null);
						output.put("sessionId", e.getSessionId().toString());
						output.put("timestamp", e.getTimestamp());
						output.set("metaJson", metaNode); // 문자열이 아닌 JsonNode로 넣음

						// pretty print로 JSON 문자열 생성
						return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
					} catch (Exception ex) {
						return "{}";
					}
				}))
			.apply("WriteExtractedToDL", TextIO.write().to(pathPrefix)
				.withSuffix(".json")
				.withoutSharding());
	}

}
