CREATE DATABASE product_service_db;
CREATE DATABASE recommend_service_db;

\c recommend_service_db

-- pgvector 확장 설치
CREATE EXTENSION IF NOT EXISTS vector;
