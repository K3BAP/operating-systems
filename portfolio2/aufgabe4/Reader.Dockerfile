FROM gradle:jdk21-alpine
WORKDIR /app
COPY src src
COPY build.gradle .
RUN gradle build
CMD ["gradle", "runReader"]