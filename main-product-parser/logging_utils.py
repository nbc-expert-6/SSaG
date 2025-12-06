import logging

# Logging 설정
def setup_logger():
    logging.basicConfig(
        level=logging.INFO,  # INFO 이상 레벨만 출력
        format="%(asctime)s [%(levelname)s] %(message)s",
        handlers=[
            logging.StreamHandler()  # 콘솔 출력
        ]
    )
