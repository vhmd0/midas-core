# syntax=docker/dockerfile:1

################################################################################
# Stage 1: Download dependencies
################################################################################
FROM eclipse-temurin:17-jdk-jammy as deps

WORKDIR /build

# Copy Maven wrapper and config
COPY --chmod=0755 mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Ensure Linux line endings (important if building on Windows)
RUN sed -i 's/\r$//' mvnw

# Download dependencies, cache ~/.m2
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

################################################################################
# Stage 2: Build the application
################################################################################
FROM deps as package

WORKDIR /build

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################
# Stage 3: Extract Spring Boot layers
################################################################################
FROM package as extract

WORKDIR /build
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################
# Stage 4: Final runtime image
################################################################################
FROM eclipse-temurin:17-jre-jammy AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

WORKDIR /app

COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]