FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY src/main/resources/python/requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

COPY build/libs/*.jar AnalysisService.jar
COPY src/main/resources/python/kmeans.py /app/python/kmeans.py

CMD ["sh", "-c", "mkdir -p /tmp/extracted && exec java -jar /app/AnalysisService.jar"]