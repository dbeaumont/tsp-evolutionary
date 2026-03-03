# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Utilisation du proxy nexus : maven
ARG USE_NEXUS_MAVEN=false
COPY settings.xml /tmp/settings.xml
RUN if [ "$USE_NEXUS_MAVEN" = "true" ]; then mkdir -p /root/.m2 && mv /tmp/settings.xml /root/.m2/settings.xml; else rm /tmp/settings.xml; fi

COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
