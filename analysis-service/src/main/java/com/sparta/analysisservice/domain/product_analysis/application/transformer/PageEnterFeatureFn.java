package com.sparta.analysisservice.domain.product_analysis.application.transformer;

import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.transforms.DoFn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.application.extractor.BaseExtractor;

public class PageEnterFeatureFn extends DoFn<BaseExtractor.Extracted, BaseExtractor.Extracted> {

	private final ObjectMapper mapper = new ObjectMapper();

	@ProcessElement
	public void process(ProcessContext context) throws Exception {
		BaseExtractor.Extracted ex = context.element();
		Map<String, Object> meta;
		if (ex.getMetaJson() == null || ex.getMetaJson().isBlank()) {
			meta = new HashMap<>();
		} else {
			String json = ex.getMetaJson();
			if (json.startsWith("{")) {
				meta = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
			} else {
				meta = new HashMap<>();
				meta.put("raw", json);  // 혹은 path 값으로 저장
			}
		}

		meta.put("feature_page_enter_flag", 1);
		meta.put("enterTimestamp", ex.getTimestamp());

		BaseExtractor.Extracted out = new BaseExtractor.Extracted(
			ex.getEventType(),
			ex.getProductId(),
			ex.getSessionId(),
			ex.getTimestamp(),
			mapper.writeValueAsString(meta)
		);

		context.output(out);
	}

}
