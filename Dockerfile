# ===== Stage 1: build =====
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Сначала копируем только обёртку и pom — для кеша зависимостей
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Затем исходники и сборка
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ===== Stage 2: runtime =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Непривилегированный пользователь
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
