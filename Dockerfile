FROM --platform=linux/amd64 gradle:8.4-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle

RUN gradle dependencies || return 0

COPY . .
RUN gradle build -x test

FROM --platform=linux/amd64 openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/build/libs/shopping-event-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
