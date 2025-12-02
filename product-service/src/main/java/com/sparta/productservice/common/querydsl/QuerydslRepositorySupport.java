package com.sparta.productservice.common.querydsl;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

@Repository
public abstract class QuerydslRepositorySupport {

	private final Class<?> domainClass;
	private Querydsl querydsl;
	private EntityManager entityManager;
	private JPAQueryFactory queryFactory;

	protected QuerydslRepositorySupport(Class<?> domainClass) {
		Assert.notNull(domainClass, "Domain class must not be null!");
		this.domainClass = domainClass;
	}

	@Autowired
	public void setEntityManager(EntityManager entityManager) {
		Assert.notNull(entityManager, "EntityManager must not be null!");

		JpaEntityInformation<?, ?> entityInformation =
			JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);

		PathBuilder<?> builder = new PathBuilder<>(
			entityInformation.getJavaType(),
			entityInformation.getJavaType().getSimpleName()
		);

		this.entityManager = entityManager;
		this.querydsl = new Querydsl(entityManager, builder);
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	@PostConstruct
	public void validate() {
		Assert.notNull(entityManager, "EntityManager must not be null!");
		Assert.notNull(querydsl, "Querydsl must not be null!");
		Assert.notNull(queryFactory, "QueryFactory must not be null!");
	}

	protected JPAQueryFactory getQueryFactory() {
		return queryFactory;
	}

	protected Querydsl getQuerydsl() {
		return querydsl;
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected <T> JPAQuery<T> select(Expression<T> expr) {
		return getQueryFactory().select(expr);
	}

	protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
		return getQueryFactory().selectFrom(from);
	}

	/**
	 * 페이지네이션을 포함한 QueryDsl 리스트 조회
	 * Count 쿼리를 자동으로 실행하지 않는 최적화 버전
	 *
	 * @param pageable     Pageable 객체
	 * @param contentQuery 조회 lambda query
	 * @return Page<T> 객체
	 * @author skfkgla
	 */
	protected <T> Page<T> applyPagination(
		Pageable pageable,
		Function<JPAQueryFactory, JPAQuery<T>> contentQuery
	) {
		JPAQuery<T> jpaQuery = contentQuery.apply(getQueryFactory());
		List<T> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();

		return PageableExecutionUtils.getPage(content, pageable, jpaQuery::fetchCount);
	}

	/**
	 * 페이지네이션을 포함한 QueryDsl 리스트 조회
	 * Count 쿼리를 별도로 지정하는 버전
	 *
	 * @param pageable     Pageable 객체
	 * @param contentQuery 조회 lambda query
	 * @param countQuery   카운트 lambda query
	 * @return Page<T> 객체
	 * @author skfkgla
	 */
	protected <T> Page<T> applyPagination(
		Pageable pageable,
		Function<JPAQueryFactory, JPAQuery<T>> contentQuery,
		Function<JPAQueryFactory, JPAQuery<Long>> countQuery
	) {
		JPAQuery<T> jpaContentQuery = contentQuery.apply(getQueryFactory());
		List<T> content = getQuerydsl().applyPagination(pageable, jpaContentQuery).fetch();

		JPAQuery<Long> countResult = countQuery.apply(getQueryFactory());

		return PageableExecutionUtils.getPage(content, pageable, countResult::fetchOne);
	}
}