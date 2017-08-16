# Context - root of java project
# Build Stage
FROM openjdk:8u131-jdk AS build
COPY ./ /cm
WORKDIR /cm
RUN ./gradlew clean :connectors:installDist

# Container with application
FROM openjdk:8u131-jre-alpine
COPY --from=build /cm/connectors/build/install/connectors /cm/connectors/dist
WORKDIR /cm/connectors/dist
CMD ["./bin/connectors"]



