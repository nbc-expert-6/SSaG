package com.sparta.analysisservice.domain.product_analysis.application.transformer;

import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.transforms.DoFn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.application.extractor.BaseExtractor;

public class PageExitFeatureFn extends DoFn<BaseExtractor.Extracted, BaseExtractor.Extracted> {
	private final ObjectMapper mapper = new ObjectMapper();

	@ProcessElement
	public void process(ProcessContext c) throws Exception {
		BaseExtractor.Extracted ex = c.element();
		Map<String, Object> meta = new HashMap<>();
		String metaJson = ex.getMetaJson();
		if (metaJson != null) {
			try {
				meta = mapper.readValue(metaJson, Map.class);
			} catch (JsonProcessingException e) {
				meta.put("meta", metaJson);
			}
		}

		/*if (meta.containsKey("enterTimestamp")) {
			Long enterTs = Long.parseLong(meta.get("enterTimestamp").toString());
			Long duration = ex.getTimestamp() - enterTs;
			meta.put("feature_page_duration", duration);
		}*/

		meta.put("feature_page_exit_flag", 1);

		BaseExtractor.Extracted out = new BaseExtractor.Extracted(
			ex.getEventType(), ex.getProductId(),
			ex.getSessionId(), ex.getTimestamp(),
			mapper.writeValueAsString(meta)
		);

		c.output(out);
	}
}
