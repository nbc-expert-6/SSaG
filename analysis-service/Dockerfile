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

COPY src/main/resources/python/requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

COPY --from=builder /build/build/libs/*.jar app.jar

COPY src/main/resources/python/kmeans.py /app/python/kmeans.py

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "mkdir -p /tmp/extracted && java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]