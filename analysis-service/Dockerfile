FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY build/libs/*.jar AnalysisService.jar
ENTRYPOINT ["java","-jar","AnalysisService.jar"]