import json
import numpy as np
import os
import pandas as pd
import requests
from glob import glob
from kneed import KneeLocator
from sklearn.cluster import KMeans, DBSCAN
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import StandardScaler

# 1) 날짜별 JSON 파일 경로
enter_folder = "/tmp/extracted/enter"
exit_folder = "/tmp/extracted/exit"
session_folder = "/tmp/extracted/session"


def load_all_json(folder_path):
    """폴더 안의 모든 JSON 파일 읽어서 리스트로 반환"""
    all_data = []
    for file_path in glob(os.path.join(folder_path, "*.json")):
        print("Loading file:", os.path.basename(file_path))
        with open(file_path, "r", encoding="utf-8") as f:
            all_data.extend(json.load(f))
    return all_data


# 2) 모든 날짜 파일 읽기
enter_data = load_all_json(enter_folder)
exit_data = load_all_json(exit_folder)
session_data = load_all_json(session_folder)

# 3) 데이터 합치기
all_data = enter_data + exit_data + session_data

# 4) DataFrame으로 변환
df = pd.json_normalize(all_data)

# 5) 세션별 의미 있는 feature 추가
session_group = (
    df.groupby("sessionId")
    .agg(
        enter_count=("metaJson.feature_page_enter_flag", "sum"),
        exit_count=("metaJson.exitTimestamp", lambda x: x.notna().sum()),
        total_duration=("metaJson.feature_page_duration", "sum"),
        avg_duration=("metaJson.feature_page_duration", "mean"),
        max_duration=("metaJson.feature_page_duration", "max"),
        min_duration=("metaJson.feature_page_duration", "min"),
    )
    .fillna(0)
)

# 기존 df에 병합
df = df.merge(session_group, on="sessionId", how="left")

# 6) KMeans에 사용할 feature 선택
features = [
    "enter_count",  # 해당 세션 내 PAGE_ENTER 횟수
    "exit_count",  # 해당 세션 내 PAGE_EXIT 횟수
    "total_duration",  # 세션 내 총 체류 시간
    "avg_duration",  # 세션 평균 체류 시간
    "max_duration",  # 세션 최대 체류 시간
    "min_duration",  # 세션 최소 체류 시간
]

# 결측치 0으로 채움
for f in features:
    if f not in df.columns:
        df[f] = 0
X = df[features].fillna(0)

n_samples = X.shape[0]
if n_samples < 2:
    raise RuntimeError(f"Not enough samples for clustering: {n_samples}")

# 7) 스케일링
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

max_k = min(10, n_samples)
k_range = range(1, max_k + 1)

# 8) Elbow 방법으로 최적의 군집 수 K 찾기
wcss = []
k_range = range(1, 11)  # k=1~10까지 확인
for k in k_range:
    km = KMeans(n_clusters=k, random_state=42)
    km.fit(X_scaled)
    wcss.append(km.inertia_)  # inertia_: 군집 내 제곱합 (WCSS)

kl = KneeLocator(k_range, wcss, curve="convex", direction="decreasing")
optimal_k = kl.elbow or min(3, n_samples)

optimal_k = min(optimal_k, n_samples)

# 9) KMeans 군집화
kmeans = KMeans(n_clusters=optimal_k, random_state=42)
df["cluster"] = kmeans.fit_predict(X_scaled)

# 10) DBSCAN으로 이상 세션 탐지 (봇 / 자동화 탐지용)

min_samples = X_scaled.shape[1] + 1

neighbors = NearestNeighbors(n_neighbors=min_samples)
neighbors_fit = neighbors.fit(X_scaled)
distances, _ = neighbors_fit.kneighbors(X_scaled)
k_distances = np.sort(distances[:, -1])

diffs = np.diff(k_distances)
elbow_idx = np.argmax(diffs)
eps = k_distances[elbow_idx]
print(f"Estimated eps: {eps:.3f}, min_samples: {min_samples}")

dbscan = DBSCAN(eps=eps, min_samples=min_samples)
db_labels = dbscan.fit_predict(X_scaled)
df["is_anomalous"] = db_labels == -1

# 5 ~ 15%면 적당
anomaly_ratio = df["is_anomalous"].mean() * 100
print(f"Detected anomalies: {df['is_anomalous'].sum()}, Ratio: {anomaly_ratio:.2f}%")

# 11) Java로 넘길 데이터 생성
session_clusters = (
    df[["sessionId", "cluster", "is_anomalous"]]
    .drop_duplicates()
    .to_dict(orient="records")
)

cluster_profiles = (
    df.groupby("cluster")[features].mean().reset_index().to_dict(orient="records")
)

payload = {"sessions": session_clusters, "clusterProfiles": cluster_profiles}

ANALYSIS_SERVER_URL = "http://analysis-service:8082/api/v1/session-cluster"

response = requests.post(ANALYSIS_SERVER_URL, json=payload, timeout=5)

if response.status_code != 200:
    raise RuntimeError(f"Failed to send cluster result: {response.status_code}")

print("Payload to send:", json.dumps(payload, indent=2))
