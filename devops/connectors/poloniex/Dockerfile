# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :connectors:poloniex:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/connectors/poloniex/build/install/poloniex /cyberapp
ENTRYPOINT ["/cyberapp/bin/poloniex"]
