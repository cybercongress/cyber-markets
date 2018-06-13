# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /build
WORKDIR /build
RUN ./gradlew clean :storer:assemble

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /build/storer/build/libs /cyberapp/bin

ENTRYPOINT exec java $JAVA_OPTS -jar /cyberapp/bin/storer.jar
