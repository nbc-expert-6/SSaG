# -*- coding: utf-8 -*-
import json
import psycopg2  # Python에서 PostgreSQL 데이터베이스 접속
import uuid
from datetime import datetime
from gensim.models import Word2Vec  # 벡터 임베딩
from itertools import groupby  # 연속된 동일 값들을 그룹화할때
from kafka import KafkaConsumer
from operator import itemgetter  # 특정 항목을 기준으로 정렬/추출
from psycopg2.extras import execute_values  # Postgre에 대량 데이터를 효율적으로 삽입

# -----------------------------
# 1) PostgreSQL 연결 설정
# -----------------------------
conn = psycopg2.connect(
    host="localhost",
    port=5433,
    dbname="recommend_service_db",
    user="postgres",
    password="qwer1234!",
)


# -----------------------------
# 2) Kafka Consumer 설정
# -----------------------------
def fetch_recent_events(batch_size=1000):
    consumer = KafkaConsumer(
        "product.analysis",
        bootstrap_servers="localhost:9092",
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda m: json.loads(m.decode("utf-8")),
        consumer_timeout_ms=10000
    )

    events = []
    for message in consumer:
        raw = message.value

        try:
            msg = json.loads(raw)
        except Exception as e:
            print(f"[JSON ERROR] raw={raw}, error={e}")
            continue

        events.append(msg)

        # batch_size가 되면 yield
        if len(events) >= batch_size:
            print(f"[BATCH READY] size={len(events)}")
            yield events
            events = []  # reset

    # 마지막 남은 데이터도 처리
    if events:
        print(f"[FINAL BATCH] size={len(events)}")
        yield events

    consumer.close()
    return events


# -----------------------------
# 3) 이벤트 로그 → 세션별 클릭 시퀀스 생성
# -----------------------------
def build_sequences(logs):
    print("Sorting logs...")

    logs_sorted = sorted(logs, key=itemgetter("sessionId", "clickedAt"))

    sequences = []
    for session_id, items in groupby(logs_sorted, key=itemgetter("sessionId")):
        seq = [item["productId"] for item in items]
        if len(seq) >= 2:
            sequences.append(seq)

    return sequences


# -----------------------------
# 4) Word2Vec 학습
# -----------------------------
def train_item2vec(sequences):
    model = Word2Vec(
        sentences=sequences, vector_size=128, window=5, min_count=1, sg=1, workers=8
    )
    print("Word2Vec model trained!")
    return model


# -----------------------------
# 5) 학습된 벡터를 PostgreSQL에 저장
# -----------------------------
def save_vectors_to_pg(model):
    now = datetime.now()
    with conn.cursor() as cur:
        data = []
        for pid in model.wv.index_to_key:
            vec = model.wv[pid].tolist()
            # pgvector는 문자열 '[v1,v2,...]' 형태도 파싱 가능
            vec_str = "[" + ",".join(str(v) for v in vec) + "]"

            data.append((pid, vec_str, now))
        # upsert
        execute_values(
            cur,
            """
            INSERT INTO p_product_vector (product_id, embedding, updated_at)
            VALUES %s ON CONFLICT (product_id) DO
            UPDATE
                SET embedding = EXCLUDED.embedding,
                updated_at = EXCLUDED.updated_at
            """,
            data,
            template="(%s, %s, %s)",
        )
    conn.commit()
    print("Insert to p_product_vector successful!")


# -----------------------------
# 6) 배치 처리 함수
# -----------------------------
def process_batch_events(logs):
    sequences = build_sequences(logs)
    if not sequences:
        print("No valid sequences found")
        return

    model = train_item2vec(sequences)
    save_vectors_to_pg(model)


# -----------------------------
# 7) 메인 실행
# -----------------------------
def main():
    print("Batch Recommendation Vector Processor started!")

    for batch_logs in fetch_recent_events(batch_size=1000):
        if not batch_logs:
            print("No events in this batch, skipping.")
            continue

        process_batch_events(batch_logs)

    print("Batch processing finished!")


# -----------------------------
# 8) 실행
# -----------------------------
if __name__ == "__main__":
    main()
