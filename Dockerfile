FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY src/ ./src/

RUN mkdir -p logs bin config && \
    javac -d bin src/Logger.java src/ClientHandler.java src/BombSquadServer.java

EXPOSE 29248

CMD ["java", "-cp", "bin", "BombSquadServer"]
