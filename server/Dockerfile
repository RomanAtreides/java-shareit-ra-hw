FROM amazoncorretto:11-alpine-jdk
WORKDIR /app
COPY target/*.jar shareit-server.jar
ENTRYPOINT ["java","-jar","./shareit-server.jar"]
