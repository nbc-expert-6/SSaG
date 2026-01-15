import logging
import threading

from prometheus_client import start_http_server

"""
Prometheus metrics HTTP server.

/metrics 엔드포인트를 8000번 포트로 노출하는 HTTP 서버를 제공.
start_metrics_server() 함수를 호출 시 백그라운드 스레드에서 실행.
"""
def start_metrics_server(port: int = 8000):
    logging.info(f"Starting Prometheus metrics server on port {port}")

    t = threading.Thread(target=start_http_server, args=(port,), daemon=True)
    t.start()
