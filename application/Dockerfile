# Etapa 1: compila el jar con Maven y JDK 17. La cache de .m2 evita descargar las dependencias
# cada vez que cambia el codigo. Las pruebas se ejecutan aparte (mvnw test, mvnw verify).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

# Etapa 2: solo el JRE 17 y el jar, con un usuario sin privilegios de root.
FROM eclipse-temurin:17-jre
RUN useradd --system --no-create-home nexusmarket
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER nexusmarket
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Bogota", "-jar", "app.jar"]
