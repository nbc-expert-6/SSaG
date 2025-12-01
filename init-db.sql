CREATE DATABASE product_service_db;
CREATE DATABASE recommend_service_db;

\c recommend_service_db

-- pgvector 확장 설치
CREATE EXTENSION IF NOT EXISTS vector;

-- HNSW Index 생성
-- embedding 컬럼을 기반으로 한 벡터 유사도 검색을 빠르게 수행
CREATE INDEX idx_product_vector_embedding
ON p_product_vector
USING hnsw (embedding vector_cosine_ops);