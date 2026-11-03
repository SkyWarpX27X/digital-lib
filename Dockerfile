FROM eclipse-temurin:25-jdk AS build
COPY --from=node:22-bookworm-slim /usr/local/bin/node /usr/local/bin/node
COPY --from=node:22-bookworm-slim /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm \
    && ln -s /usr/local/lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx

WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY digital-lib-spring-boot-starter digital-lib-spring-boot-starter
COPY frontend frontend
COPY src src
RUN chmod +x gradlew && ./gradlew :bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
RUN useradd --system --create-home --uid 10001 app
WORKDIR /app
COPY --from=build /workspace/build/libs/app.jar app.jar
RUN mkdir logs && chown app:app logs
USER app
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]