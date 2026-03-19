# TP Microservices - Coworking

Architecture : Spring Boot + Spring Cloud + Kafka.

## Prerequis

- Java 17
- Docker + Docker Compose

## Services

- `config-server` (8888)
- `discovery-server` (8761)
- `api-gateway` (8080)
- `room-service` (8081)
- `member-service` (8082)
- `reservation-service` (8083)

## Lancement

Depuis la racine du projet (exemple : `/path/to/project`) :

```bash
cd "/path/to/project"
docker compose up -d
```

Puis lancer les services dans cet ordre (un terminal par service) :

```bash
cd "/path/to/project/config-server" && ./mvnw spring-boot:run
cd "/path/to/project/discovery-server" && ./mvnw spring-boot:run
cd "/path/to/project/api-gateway" && ./mvnw spring-boot:run
cd "/path/to/project/room-service" && ./mvnw spring-boot:run
cd "/path/to/project/member-service" && ./mvnw spring-boot:run
cd "/path/to/project/reservation-service" && ./mvnw spring-boot:run
```

## Verification rapide

- `http://localhost:8888/actuator/health`
- `http://localhost:8761`
- `http://localhost:8080/actuator/health`

## Documentation API (Swagger)

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`

## Tests Postman

Importer :

- `postman/Coworking-TP-Step5.postman_collection.json`

## Arret

```bash
cd "/path/to/project"
docker compose down
```
