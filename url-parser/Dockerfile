FROM python:3.11-slim

RUN apt-get update && apt-get install -y \
    wget gnupg unzip curl \
    chromium chromium-driver \
    libnss3 libxi6 libgbm-dev libasound2 \
    fonts-liberation libappindicator3-1 libxshmfence1 libxss1 xvfb \
    && rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/bin/chromium /usr/bin/google-chrome || true

WORKDIR /app
ENV PYTHONBUFFERED=1
ENV PYTHONPATH=/app

ENV DISPLAY=:99
ENV DBUS_SESSION_BUS_ADDRESS=/dev/null

ARG PLATFORM=auction
ENV PLATFORM=${PLATFORM}

COPY common /app/common

COPY crawler/url_parser.py /app/crawler/url_parser.py

COPY crawler/${PLATFORM} /app/crawler/${PLATFORM}

COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt

RUN mkdir -p /root/.local/share/undetected_chromedriver && \
    chmod -R 755 /root/.local/share/undetected_chromedriver

CMD Xvfb :99 -screen 0 1280x720x24 -ac +extension GLX +render -noreset & \
    python crawler/${PLATFORM}/main.py
