# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :tickers:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/tickers/build/install/tickers /cyberapp

ENTRYPOINT ["/cyberapp/bin/tickers"]