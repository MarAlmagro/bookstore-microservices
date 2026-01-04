FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/admin-ui-*.jar app.jar

EXPOSE 8090

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
