FROM gradle:8.1-jdk11 as build_stage
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN ./gradlew clean build

FROM openjdk:11-slim
WORKDIR /app
ENV PROFILE=prod
COPY --from=build_stage /home/gradle/project/build/libs/crypto-recommendation-service.jar .

EXPOSE 8080

ENTRYPOINT ["java","-jar","crypto-recommendation-service.jar"]
