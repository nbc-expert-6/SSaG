package com.sparta.analysisservice.domain.product_analysis.application.service;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.application.extractor.BaseExtractor;
import com.sparta.analysisservice.domain.product_analysis.application.transformer.DurationFn;
import com.sparta.analysisservice.domain.product_analysis.application.transformer.PageEnterFeatureFn;
import com.sparta.analysisservice.domain.product_analysis.application.transformer.PageExitFeatureFn;
import com.sparta.analysisservice.domain.product_analysis.application.transformer.UserEventTransformFn;
import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.beam.EtlPipeline;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventEtlService {

	private final EtlPipeline pipeline;

	@Autowired
	private ElasticsearchIO.ConnectionConfiguration esConnConfig;

	public void runEtlJob() {
		Pipeline p = pipeline.create();

		// 1) ES에서 읽어오기
		PCollection<UserEventDocument> events = pipeline.readFromElastic(p, esConnConfig);

		// 2) Transform
		PCollection<BaseExtractor.Extracted> extracted =
			events.apply("TransformToExtracted", ParDo.of(new UserEventTransformFn()));

		// 3) 페이지 진입 / 이탈 이벤트 추가
		PCollection<BaseExtractor.Extracted> pageEnterFeats =
			extracted.apply("FilterPageEnter", Filter.by(e -> "page_enter".equalsIgnoreCase(e.getEventType())))
				.apply("PageEnterFeature", ParDo.of(new PageEnterFeatureFn()));

		PCollection<BaseExtractor.Extracted> pageExitFeats =
			extracted.apply("FilterPageExit", Filter.by(e -> "page_exit".equalsIgnoreCase(e.getEventType())))
				.apply("PageExitFeature", ParDo.of(new PageExitFeatureFn()));

		// 3-1) Duration 계산용: enter + exit 합치기
		PCollection<BaseExtractor.Extracted> merged =
			PCollectionList.of(pageEnterFeats).and(pageExitFeats)
				.apply("MergeEnterExit", Flatten.pCollections());

		// 3-2) sessionId 로 KeyBy
		PCollection<KV<String, BaseExtractor.Extracted>> keyed =
			merged.apply("KeyBySessionId",
				MapElements.into(
						TypeDescriptors.kvs(
							TypeDescriptors.strings(),
							TypeDescriptor.of(BaseExtractor.Extracted.class)))
					.via(e -> KV.of(e.getSessionId().toString(), e))
			);

		// 3-3) GroupByKey로 세션 기반 묶기
		PCollection<KV<String, Iterable<BaseExtractor.Extracted>>> grouped =
			keyed.apply("GroupBySessionId", GroupByKey.create());

		// 3-4) DurationFn 적용
		PCollection<BaseExtractor.Extracted> sessionDurations =
			grouped.apply("ComputeDuration", ParDo.of(new DurationFn()));

		// 4) Write to Datalake
		pipeline.writeExtractedToDataLakeAsArray(pageEnterFeats, "/tmp/extracted/enter");
		pipeline.writeExtractedToDataLakeAsArray(pageExitFeats, "/tmp/extracted/exit");
		pipeline.writeExtractedToDataLakeAsArray(sessionDurations, "/tmp/extracted/session"); // ← New!

		System.out.println("[DL WRITE] Page enter/exit features write started...");

		// 5) 실행
		p.run().waitUntilFinish();

	}

}
