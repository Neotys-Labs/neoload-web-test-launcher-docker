# Step : Test and package
FROM maven:3.6-jdk-8-alpine as target
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ /build/src/
RUN mvn package

# Step : Package image
FROM openjdk:8-jre

CMD exec java $JAVA_OPTS -jar /usr/share/neoload-web-test-launcher/neoload-web-test-launcher.jar

# Add the service itself
COPY --from=target /build/target/neoload-web-test-launcher*.jar /usr/share/neoload-web-test-launcher/neoload-web-test-launcher.jar