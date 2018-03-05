# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :connectors:bitstamp:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/connectors/bitstamp/build/install/bitstamp /cyberapp
ENTRYPOINT ["/cyberapp/bin/bitstamp"]