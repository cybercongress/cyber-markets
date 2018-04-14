# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :rest-api:assemble

# Container with application
FROM openjdk:8u131-jre-alpine
VOLUME /cyberdata
COPY --from=build /cm/rest-api/build/install/rest-api /cyberapp
ENTRYPOINT ["/cyberapp/bin/rest-api"]
