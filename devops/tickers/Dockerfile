# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /build
WORKDIR /build
RUN ./gradlew clean :tickers:assemble

# Container with application
FROM openjdk:8u131-jre-alpine
COPY --from=build /build/tickers/build/libs /cyberapp/bin

ENTRYPOINT ["java", "-jar", "/cyberapp/bin/tickers.jar"]