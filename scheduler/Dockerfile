FROM eclipse-temurin:21-jdk-jammy AS build

COPY target/scheduler.jar /home/service.jar
WORKDIR /home/
CMD java -jar service.jar