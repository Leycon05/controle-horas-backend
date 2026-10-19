FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

# Recria o application.properties diretamente no container em UTF-8 limpo
RUN echo "spring.application.name=controle-horas" > src/main/resources/application.properties && \
    echo "server.port=\${PORT:8080}" >> src/main/resources/application.properties && \
    echo "spring.datasource.url=\${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/controle_horas_db?createDatabaseIfNotExist=true&serverTimezone=America/Sao_Paulo&useSSL=false&allowPublicKeyRetrieval=true}" >> src/main/resources/application.properties && \
    echo "spring.datasource.username=\${SPRING_DATASOURCE_USERNAME:root}" >> src/main/resources/application.properties && \
    echo "spring.datasource.password=\${SPRING_DATASOURCE_PASSWORD:messi}" >> src/main/resources/application.properties && \
    echo "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver" >> src/main/resources/application.properties && \
    echo "spring.jpa.hibernate.ddl-auto=update" >> src/main/resources/application.properties && \
    echo "spring.jpa.show-sql=false" >> src/main/resources/application.properties && \
    echo "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect" >> src/main/resources/application.properties

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/controle-horas-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]