FROM gradle:7.6.1-jdk11 as build_stage
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN ./gradlew build

FROM eclipse-temurin:11-jre
WORKDIR /app
ENV PROFILE=prod
COPY --from=build_stage /home/gradle/project/build/libs/crypto-recommendation-service.jar .

EXPOSE 8080

ENTRYPOINT ["java","-jar","crypto-recommendation-service.jar"]
