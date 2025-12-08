FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || return 0

COPY . .
RUN gradle clean build -x test --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]