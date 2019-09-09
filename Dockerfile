# Step : Test and package
FROM maven:3.6-jdk-11 as target

WORKDIR /build
COPY pom.xml /build
RUN mvn dependency:go-offline
COPY /src /build/src/
RUN mvn package

# Step : Package image
FROM openjdk:11-jre

CMD exec java $JAVA_OPTS -jar /usr/share/neoload-web-test-launcher/neoload-web-test-launcher.jar

# Add the service itself
COPY --from=target /build/target/neoload-web-test-launcher-docker-?.?.?.jar /usr/share/neoload-web-test-launcher/neoload-web-test-launcher.jar