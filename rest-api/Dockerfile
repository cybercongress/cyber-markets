# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /build
WORKDIR /build
RUN ./gradlew clean :rest-api:assemble

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /build/rest-api/build/libs /cyberapp/bin

ENTRYPOINT exec java $JAVA_OPTS -jar /cyberapp/bin/rest-api.jar
