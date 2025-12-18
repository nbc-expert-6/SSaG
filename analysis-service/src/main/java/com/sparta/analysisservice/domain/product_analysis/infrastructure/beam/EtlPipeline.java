package com.sparta.analysisservice.domain.product_analysis.infrastructure.beam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Combine;
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
	public PCollection<UserEventDocument> readFromElastic(Pipeline p,
		ElasticsearchIO.ConnectionConfiguration connConfig) {
		/*ElasticsearchIO.ConnectionConfiguration connectionConfiguration =
			ElasticsearchIO.ConnectionConfiguration.create(
				new String[] {esHost},
				esIndex
			);*/

		PCollection<String> jsons = p.apply("ReadFromEs",
			ElasticsearchIO.read()
				.withConnectionConfiguration(connConfig)
		);

		return jsons
			.apply("JsonToUserEvent", ParDo.of(new JsonToUserEventFn()))
			.setCoder(SerializableCoder.of(UserEventDocument.class));
	}

	public static class JsonArrayCombineFn extends Combine.CombineFn<String, List<String>, String> {

		@Override
		public List<String> createAccumulator() {
			return new ArrayList<>();
		}

		@Override
		public List<String> addInput(List<String> accumulator, String input) {
			accumulator.add(input);
			return accumulator;
		}

		@Override
		public List<String> mergeAccumulators(Iterable<List<String>> accumulators) {
			List<String> merged = new ArrayList<>();
			for (List<String> acc : accumulators) {
				merged.addAll(acc);
			}
			return merged;
		}

		@Override
		public String extractOutput(List<String> accumulator) {
			return "[" + String.join(",", accumulator) + "]";
		}
	}

	// Extracted -> DataLake(간단 JSON lines) 저장
	public void writeExtractedToDataLakeAsArray(PCollection<BaseExtractor.Extracted> extracted,
		String outputFilePath) {

		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String filePath = outputFilePath + "/" + today;

		// 1) 각 객체를 JSON 문자열로 변환
		PCollection<String> jsonStrings = extracted.apply("ExtractedToJSON", MapElements.into(TypeDescriptors.strings())
			.via(e -> {
				ObjectMapper mapper = new ObjectMapper();
				try {
					JsonNode metaNode = mapper.readTree(e.getMetaJson());

					ObjectNode output = mapper.createObjectNode();
					output.put("eventType", e.getEventType());
					output.put("productId", e.getProductId() != null ? e.getProductId().toString() : null);
					output.put("sessionId", e.getSessionId().toString());
					output.put("timestamp", e.getTimestamp());
					output.set("metaJson", metaNode);

					// compact JSON (한 줄)
					return mapper.writeValueAsString(output);

				} catch (Exception ex) {
					return "{}";
				}
			}));

		// 2) PCollection 전체를 List로 모아 JSON Array 형태로 변환
		PCollection<String> jsonArray = jsonStrings
			.apply("CombineToList", Combine.globally(new JsonArrayCombineFn()).withoutDefaults());

		// 3) 파일로 저장
		jsonArray.apply("WriteArrayToFile", TextIO.write()
			.to(filePath)
			.withoutSharding()
			.withSuffix(".json"));
	}

}
