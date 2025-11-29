# -*- coding: utf-8 -*-
import psycopg2  # Python에서 PostgreSQL 데이터베이스 접속
import uuid
from gensim.models import Word2Vec  # 벡터 임베딩
from itertools import groupby  # 연속된 동일 값들을 그룹화할때
from operator import itemgetter  # 특정 항목을 기준으로 정렬/추출
from psycopg2.extras import execute_values  # Postgre에 대량 데이터를 효율적으로 삽입

# 1) DB 연결 설정
conn = psycopg2.connect(
    host="localhost",
    port=5433,
    dbname="recommend_service_db",
    user="postgres",
    password="qwer1234!",
)


# DB 연결 테스트용 함수
def test_postgres_connection():
    """PostgreSQL 연결 테스트 함수"""
    try:
        conn = psycopg2.connect(
            host="localhost",
            port=5433,
            dbname="recommend_service_db",
            user="postgres",
            password="qwer1234!",
        )
        print("PostgreSQL connection successful!")

        # 간단한 테스트 쿼리 실행
        with conn.cursor() as cur:
            cur.execute("SELECT 1;")
            result = cur.fetchone()
            print("Test query result:", result)

            cur.execute(
                "INSERT INTO recommend_service_db.test_entity (id, name) VALUES (1, 'brian');",
            )
            conn.commit()

    except Exception as e:
        print("Error connecting to PostgreSQL:", e)

    finally:
        if "conn" in locals() and conn:
            conn.close()


# 2) 클릭 이벤트 데이터를 불러와서 Pyhton 리스트로 변환
# 추후 분석 서버 만들 시 이 부분 수정 가능
def load_logs():
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT session_id, product_id, ts
            FROM user_product_event
            WHERE event_type = 'PRODUCT_CLICK'
            """
        )
        rows = cur.fetchall()
        logs = [{"session_id": r[0], "product_id": r[1], "ts": r[2]} for r in rows]
    return logs


def load_sample_logs():
    # 샘플 UUID 생성
    s1 = str(uuid.uuid4())
    s2 = str(uuid.uuid4())
    p101 = str(uuid.uuid4())
    p102 = str(uuid.uuid4())
    p201 = str(uuid.uuid4())
    p202 = str(uuid.uuid4())

    logs = [
        {"session_id": s1, "product_id": p101, "ts": "2025-11-28 10:00:00"},
        {"session_id": s1, "product_id": p102, "ts": "2025-11-28 10:01:00"},
        {"session_id": s2, "product_id": p201, "ts": "2025-11-28 10:05:00"},
        {"session_id": s2, "product_id": p202, "ts": "2025-11-28 10:06:00"},
    ]
    return logs


# 3) 세션별 클릭 로그를 순서대로 정리해서 상품 시퀀스를 만드는 함수
def build_sequences(logs):
    # session_id, ts 기준 정렬
    logs_sorted = sorted(logs, key=itemgetter("session_id", "ts"))
    sequences = []
    for user, items in groupby(logs_sorted, key=itemgetter("session_id")):
        seq = [item["product_id"] for item in items]  # [101,102,103]
        # 너무 짧은 시퀀스는 버릴 수도 있음 (예: 길이 1)
        if len(seq) >= 2:
            sequences.append(seq)
  
    return sequences


# 4) p_product_anaylsis 테이블에 저장
def save_sequences_to_pg(logs):
    session_map = {}

    for log in logs:
        sid = str(uuid.UUID(log["session_id"]))
        pid = str(uuid.UUID(log["product_id"]))

        print("sid:", sid, "pid:", pid)

        # UUID가 아닌 값 필터링
        if not sid or not pid:
            continue

        session_map.setdefault(sid, []).append(pid)

    data = []
    for sid, seq in session_map.items():
        if len(seq) >= 2:
            data.append((sid, seq))

    if not data:
        print("No valid data to insert")
        return

    with conn.cursor() as cur:
        execute_values(
            cur,
            """
            INSERT INTO p_product_analysis (session_id, click_sequence)
            VALUES %s ON CONFLICT (session_id) DO
            UPDATE
                SET click_sequence = EXCLUDED.click_sequence
            """,
            data,
            template="(%s, %s::uuid[])",
        )
    conn.commit()
    print("Insert to p_product_analysis successful!")


# 5) 상품 시퀀스를 모델에 학습하여 각 상품을 벡터로 임베딩하는 함수
def train_item2vec(sequences):
    model = Word2Vec(
        sentences=sequences,
        vector_size=128,  # 128차원
        window=5,  # 앞뒤 상품 5개까지 문맥으로 고려
        min_count=1,  # 자주 안 나오는 상품도 포함하고 싶으면 1
        sg=1,  # 중심 단어로 단어 예측
        workers=8,  # CPU 코어 수에 맞춰 병렬 처리 가능 -> 학습 속도 향상
    )
    print("model trained!")
    print(model)
    return model


# 6) 학습된 모델의 상품 벡터를 PostgreSQL에 저장하는 함수
def save_vectors_to_pg(model):
    with conn.cursor() as cur:
        data = []
        for pid in model.wv.index_to_key:
            vec = model.wv[pid].tolist()
            # pgvector는 문자열 '[v1,v2,...]' 형태도 파싱 가능
            vec_str = "[" + ",".join(str(v) for v in vec) + "]"
            data.append((pid, vec_str))
        # upsert
        execute_values(
            cur,
            """
            INSERT INTO p_product_vector (product_id, embedding)
            VALUES %s ON CONFLICT (product_id) DO
            UPDATE
                SET embedding = EXCLUDED.embedding
            """,
            data,
        )
    conn.commit()
    print("Insert to p_product_vector successful!")


# 실행 메인 함수
if __name__ == "__main__":
    logs = load_sample_logs()
    sequences = build_sequences(logs)
    save_sequences_to_pg(logs)
    model = train_item2vec(sequences)
    save_vectors_to_pg(model)
