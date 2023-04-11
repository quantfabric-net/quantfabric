FROM maven:3.8.6-eclipse-temurin-11-alpine

WORKDIR /home/quantfabric/

COPY ./ /home/quantfabric/

ENTRYPOINT mvn clean package && java -jar quantfabric-server/target/quantfabric-server-0.0.1-SNAPSHOT.jar