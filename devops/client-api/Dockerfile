# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :client-api:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/client-api/build/install/client-api /cyberapp
ENTRYPOINT ["/cyberapp/bin/client-api"]



