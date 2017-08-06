# Context - root of java project
# Build Stage
FROM cm/java-base:1.0.0 AS build
COPY . /usr/src/project
WORKDIR /usr/src/project
RUN ./gradlew clean :connectors:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
WORKDIR /usr/src/connector
COPY --from=build /usr/src/project/connectors/build/install/connectors .
CMD ["./bin/connectors"]



