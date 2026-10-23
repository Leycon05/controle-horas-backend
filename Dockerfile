FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

# Compila o projeto utilizando o application.properties do repositório
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/controle-horas-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000

# Repassa a porta do Render e limita a memória RAM para a instância gratuita
ENTRYPOINT ["java", "-Xmx384m", "-Dserver.port=${PORT:10000}", "-jar", "app.jar"]