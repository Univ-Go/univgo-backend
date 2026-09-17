# Render has no native Java runtime, so the service builds from this image.
# Stage 1 compiles with the JDK and the Gradle wrapper pinned in the repo.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Dependency layer first: it only changes when the build scripts change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# Stage 2 keeps only the jar and a JRE.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/univgo-backend.jar app.jar

# The container gets 512 MB on the free plan; leave headroom outside the heap.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"

# Render injects PORT; application.yml reads it.
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
