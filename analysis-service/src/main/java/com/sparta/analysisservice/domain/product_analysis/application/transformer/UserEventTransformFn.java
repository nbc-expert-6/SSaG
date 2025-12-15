package com.sparta.analysisservice.domain.product_analysis.application.transformer;

import org.apache.beam.sdk.transforms.DoFn;

import com.sparta.analysisservice.domain.product_analysis.application.extractor.BaseExtractor;
import com.sparta.analysisservice.domain.product_analysis.application.extractor.PageEnterExtractor;
import com.sparta.analysisservice.domain.product_analysis.application.extractor.PageExitExtractor;
import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

public class UserEventTransformFn extends DoFn<UserEventDocument, BaseExtractor.Extracted> {

	@ProcessElement
	public void process(ProcessContext context) {
		UserEventDocument event = context.element();
		BaseExtractor.Extracted extracted = null;

		switch (event.getEventType().toLowerCase()) {
			case "page_enter":
				extracted = PageEnterExtractor.extractStatic(event);
				break;
			case "page_exit":
				extracted = PageExitExtractor.extractStatic(event);
				break;
			default:
				System.out.println("[TransformFn] Unsupported Event: " + event);
		}

		if (extracted != null) {
			System.out.println("[TransformFn] " + extracted);
			context.output(extracted);
		}
	}
}


