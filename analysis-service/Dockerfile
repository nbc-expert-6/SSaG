FROM eclipse-temurin:17-jdk
RUN apt update && apt install -y jq
RUN mkdir -p /tmp/extracted
VOLUME /tmp
COPY build/libs/*.jar AnalysisService.jar
ENTRYPOINT ["java","-jar","AnalysisService.jar"]