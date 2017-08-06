# Context - root of java project
FROM openjdk:8u131-jdk
COPY . /usr/src/project
WORKDIR /usr/src/project
RUN ./gradlew build clean --refresh-dependencies
