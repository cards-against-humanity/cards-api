FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY / /app
RUN mvn install -Dmaven.test.skip=true

FROM openjdk:8-jdk-alpine
COPY --from=0 /app/target/app.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 80