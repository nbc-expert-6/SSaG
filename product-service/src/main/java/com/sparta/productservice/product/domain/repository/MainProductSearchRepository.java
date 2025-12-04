package com.sparta.productservice.product.domain.repository;

import org.springframework.data.domain.Page;

import com.sparta.productservice.product.app.command.SearchMainProductCommand;
import com.sparta.productservice.product.domain.repository.dto.MainProductSearchResult;
import com.sparta.productservice.product.infra.search.document.MainProductDocument;

public interface MainProductSearchRepository {
	void save(MainProductDocument document);
	Page<MainProductSearchResult> search(SearchMainProductCommand command);
}
