package com.sparta.productservice.product.infra.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.sparta.productservice.product.infra.search.document.MainProductDocument;

@Repository
public interface EsMainProductSearchRepository extends ElasticsearchRepository<MainProductDocument, String> {
}
