package com.sparta.analysisservice.domain.product_analysis.application.transformer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.application.extractor.BaseExtractor;

public class DurationFn extends DoFn<KV<String, Iterable<BaseExtractor.Extracted>>, BaseExtractor.Extracted> {

	private final ObjectMapper mapper = new ObjectMapper();

	@DoFn.ProcessElement
	public void process(ProcessContext c) throws Exception {

		String sessionId = c.element().getKey();
		Iterable<BaseExtractor.Extracted> events = c.element().getValue();

		List<BaseExtractor.Extracted> list = new ArrayList<>();
		events.forEach(list::add);

		list.sort(Comparator.comparingLong(BaseExtractor.Extracted::getTimestamp));

		BaseExtractor.Extracted enterEvent = null;
		BaseExtractor.Extracted exitEvent = null;

		for (BaseExtractor.Extracted e : list) {
			if ("page_enter".equalsIgnoreCase(e.getEventType()) && enterEvent == null) {
				enterEvent = e;
			}
			if ("page_exit".equalsIgnoreCase(e.getEventType())) {
				exitEvent = e;
			}
		}

		if (enterEvent != null && exitEvent != null) {

			long duration = exitEvent.getTimestamp() - enterEvent.getTimestamp();

			Map<String, Object> meta = new HashMap<>();

			if (enterEvent.getMetaJson() != null) {
				try {
					meta = mapper.readValue(enterEvent.getMetaJson(), Map.class);
				} catch (Exception e) {
					meta.put("meta", enterEvent.getMetaJson());
				}
			}

			// duration 추가
			meta.put("enterTimestamp", enterEvent.getTimestamp());
			meta.put("exitTimestamp", exitEvent.getTimestamp());
			meta.put("feature_page_duration", duration);

			BaseExtractor.Extracted out = new BaseExtractor.Extracted(
				"page_session",
				enterEvent.getProductId(),
				UUID.fromString(sessionId),
				exitEvent.getTimestamp(),
				mapper.writeValueAsString(meta)
			);

			c.output(out);
		}
	}
}


