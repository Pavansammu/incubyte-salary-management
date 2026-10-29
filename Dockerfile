# One image that serves the Angular UI and the Spring Boot API from the same port.

# 1) Build the Angular UI
FROM node:20-alpine AS ui
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production

# 2) Build the Spring Boot jar with the UI inside (classpath:/static)
FROM maven:3.9-eclipse-temurin-21 AS api
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=ui /app/frontend/dist/frontend/browser ./src/main/resources/static
# tests run in CI / locally; skipping here keeps image builds fast
RUN mvn -q -B -DskipTests package

# 3) Small runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app && mkdir -p /data && chown app:app /data
USER app
COPY --from=api /app/backend/target/salary-management-*.jar app.jar

ENV DB_PATH=/data/salary.db \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
