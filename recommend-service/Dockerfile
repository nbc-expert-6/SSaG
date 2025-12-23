FROM gradle:8.5-jdk17 AS builder

WORKDIR /build

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY . .

RUN gradle clean build -x test --no-daemon


FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY src/main/resources/vector/requirements.txt /app/requirements.txt
RUN pip3 install --default-timeout=100 --no-cache-dir -r /app/requirements.txt

COPY src/main/resources/vector /app/vector

COPY --from=builder /build/build/libs/*.jar /app/recommend-service.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/recommend-service.jar"]