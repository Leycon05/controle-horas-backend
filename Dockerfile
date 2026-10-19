FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

# Instala utilitário para sanitizar codificação de arquivos Windows/UTF-16
RUN apt-get update && apt-get install -y dos2unix iconv && \
    iconv -f UTF-16 -t UTF-8 src/main/resources/application.properties > src/main/resources/application.properties.tmp 2>/dev/null || true && \
    if [ -s src/main/resources/application.properties.tmp ]; then mv src/main/resources/application.properties.tmp src/main/resources/application.properties; else rm -f src/main/resources/application.properties.tmp; fi && \
    dos2unix src/main/resources/application.properties && \
    chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/controle-horas-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]