-- @formatter:off

-- 데이터베이스 생성
CREATE DATABASE product_service_db;
CREATE DATABASE recommend_service_db;

-- recommend_service_db에 연결
\connect recommend_service_db

-- pgvector 확장 설치
CREATE EXTENSION IF NOT EXISTS vector;

-- 테이블 생성
CREATE TABLE IF NOT EXISTS p_product_vector
(
    product_id
    UUID
    PRIMARY
    KEY,
    embedding
    vector(128) NOT NULL, updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- HNSW Index 생성
-- embedding 컬럼을 기반으로 한 벡터 유사도 검색을 빠르게 수행
CREATE INDEX IF NOT EXISTS idx_product_vector_embedding ON p_product_vector USING hnsw (embedding vector_cosine_ops);