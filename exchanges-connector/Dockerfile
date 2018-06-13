# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /build
WORKDIR /build
RUN ./gradlew clean :exchanges-connector:assemble

# Container with application
FROM openjdk:8u131-jre-alpine
COPY --from=build /build/exchanges-connector/build/libs /cyberapp/bin

ENTRYPOINT exec java $JAVA_OPTS -jar /cyberapp/bin/exchanges-connector.jar