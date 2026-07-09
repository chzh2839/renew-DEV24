# ---- Builder ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy wrapper + build files first so the dependency-resolution layer is cached
# across rebuilds that only touch application source.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar ./app.jar
RUN chown spring:spring /app/app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
