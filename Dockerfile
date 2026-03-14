FROM openjdk:17-slim

WORKDIR /app

COPY src/ ./src/
COPY config/ ./config/

RUN mkdir -p logs bin && \
    javac -d bin src/Logger.java src/ClientHandler.java src/BombSquadServer.java

EXPOSE 29248

CMD ["java", "-cp", "bin", "BombSquadServer"]
