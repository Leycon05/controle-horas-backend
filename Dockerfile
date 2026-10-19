FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

RUN printf 'spring.application.name=controle-horas\n\
server.port=${PORT:8080}\n\n\
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/controle_horas_db?createDatabaseIfNotExist=true&serverTimezone=America/Sao_Paulo&useSSL=false&allowPublicKeyRetrieval=true}\n\
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}\n\
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:messi}\n\
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver\n\n\
spring.jpa.hibernate.ddl-auto=update\n\
spring.jpa.show-sql=false\n' > src/main/resources/application.properties

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/controle-horas-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]