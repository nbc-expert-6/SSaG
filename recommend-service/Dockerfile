FROM eclipse-temurin:17-jdk-jammy


RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app


COPY src/main/resources/vector/requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

COPY src/main/resources/vector /app/vector

COPY build/libs/recommend-service-0.0.1-SNAPSHOT.jar /app/recommend-service.jar


CMD ["java", "-jar", "/app/recommend-service.jar"]
