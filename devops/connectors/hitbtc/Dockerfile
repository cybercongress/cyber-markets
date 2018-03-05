# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :connectors:hitbtc:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/connectors/hitbtc/build/install/hitbtc /cyberapp
ENTRYPOINT ["/cyberapp/bin/hitbtc"]
