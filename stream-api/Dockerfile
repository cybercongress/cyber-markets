# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :stream-api:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/stream-api/build/install/stream-api /cyberapp
ENTRYPOINT ["/cyberapp/bin/stream-api"]



