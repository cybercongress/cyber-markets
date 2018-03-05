# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :connectors:bitfinex:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/connectors/bitfinex/build/install/bitfinex /cyberapp
ENTRYPOINT ["/cyberapp/bin/bitfinex"]
