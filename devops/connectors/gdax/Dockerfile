# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :connectors:gdax:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/connectors/gdax/build/install/gdax /cyberapp
ENTRYPOINT ["/cyberapp/bin/gdax"]
