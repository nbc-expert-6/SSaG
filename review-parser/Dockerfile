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

COPY common /app/common

COPY core /app/core

COPY crawler /app/crawler

COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt

RUN mkdir -p /root/.local/share/undetected_chromedriver && \
    chmod -R 755 /root/.local/share/undetected_chromedriver

CMD Xvfb :99 -screen 0 1280x720x24 -ac +extension GLX +render -noreset & \
    python core/app/main.py
