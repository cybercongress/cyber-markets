# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /build
WORKDIR /build
RUN ./gradlew clean :tickers-lambda:assemble

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /build/tickers-lambda/build/libs /cyberapp/bin

ENTRYPOINT ["java", "-jar", "/cyberapp/bin/tickers-lambda.jar"]
