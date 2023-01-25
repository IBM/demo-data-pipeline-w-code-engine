FROM maven:3-jdk-11-openj9 AS builder

WORKDIR /app
COPY . /app

RUN mvn clean compile assembly:single

FROM adoptopenjdk:11-jre-openj9

WORKDIR /app
COPY --from=builder /app/target/demo-dataflow-1.0-SNAPSHOT-jar-with-dependencies.jar /app/dataflow.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/dataflow.jar"]
