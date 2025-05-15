FROM alpine/java:21-jdk

RUN apk update && apk add --no-cache curl
WORKDIR /eureka

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

CMD ["java", "-jar", "app.jar"]