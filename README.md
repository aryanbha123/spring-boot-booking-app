# VertexSpace Booking

A Spring Boot booking API that rate-limits requests per user and processes them
asynchronously through RabbitMQ, alongside a React + TypeScript web app scaffold.

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![React](https://img.shields.io/badge/React_19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)

## About

The repository holds two apps:

- **`vertexspace-booking-api`**: a Spring Boot service. A booking request is checked
  against a per-user rate limit, published to a RabbitMQ exchange, and picked up by a
  background worker. The HTTP call returns as soon as the request is queued.
- **`vertexspace-web-app`**: a React 19 + TypeScript + Vite frontend. It is currently
  the default Vite starter page and does not call the API yet.

## Features

- **Queue-based booking**: `POST /api/v1/booking` publishes the request to
  `booking.exchange` (topic exchange) with routing key `booking.routingKey`, bound to the
  durable queue `booking.queue`
- **Async worker**: a `@RabbitListener` consumes `booking.queue` and simulates
  processing (a 5-second delay, then a log line)
- **Rate limiting**: Bucket4j token bucket, 5 requests per minute per `userId`, held in
  memory; over the limit the API returns `429 Too Many Requests` with the wait time
- **Health check** endpoint
- **Docker Compose** stack for RabbitMQ (with the management UI), Redis and the API,
  with health checks gating the API's start
- Multi-stage `Dockerfile` (Maven build, then a slim JRE 17 image)

Redis is included in the dependencies, configuration and Compose file, but the code
does not use it yet: rate-limit buckets live in a `ConcurrentHashMap`.

## Tech Stack

| Layer | Tools |
|---|---|
| API | Java 17, Spring Boot 4.0.3 (Web MVC, AMQP, Data Redis), Bucket4j 7.6, Lombok, Maven |
| Messaging / cache | RabbitMQ 3 (management image), Redis 7 |
| Frontend | React 19, TypeScript 5.9, Vite 7, ESLint |
| Infra | Docker, Docker Compose |

## Project Structure

```
spring-boot-booking-app/
├── vertexspace-booking-api/
│   ├── src/main/java/com/vertexspace/bookingapi/
│   │   ├── config/RabbitMQConfig.java          # queue, exchange, binding
│   │   ├── controller/BookingController.java   # POST /api/v1/booking
│   │   ├── controller/HealthController.java    # GET /api/v1/health
│   │   ├── dto/BookingRequest.java             # userId, itemId, quantity
│   │   ├── service/RateLimitingService.java    # Bucket4j buckets per user
│   │   └── worker/BookingWorker.java           # RabbitMQ consumer
│   ├── src/main/resources/application.yml
│   ├── docker-compose.yml
│   └── Dockerfile
└── vertexspace-web-app/                        # React + Vite frontend
```

## API Endpoints

| Method | Route | Description |
|---|---|---|
| GET | `/api/v1/health` | Returns `{ "status": "UP", "message": "VertexSpace Booking API is running" }` |
| POST | `/api/v1/booking` | Queues a booking. `200` with remaining tokens, or `429` when rate-limited |

Example request:

```bash
curl -X POST http://localhost:8080/api/v1/booking \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-1", "itemId": "room-42", "quantity": 1}'
```

## Getting Started

### Prerequisites

- Java 17 (or Docker only, for the full-stack option)
- Docker and Docker Compose
- Node.js, for the web app

### Booking API

Full stack in Docker:

```bash
cd vertexspace-booking-api
docker-compose up --build
```

Or run RabbitMQ and Redis in Docker and the app locally:

```bash
cd vertexspace-booking-api
docker-compose up -d rabbitmq redis
./mvnw spring-boot:run
```

The API listens on http://localhost:8080 and the RabbitMQ management UI on
http://localhost:15672.

#### Environment variables

All are optional; defaults in `application.yml` target localhost.

| Name | Default |
|---|---|
| `RABBIT_HOST` | `localhost` |
| `RABBIT_PORT` | `5672` |
| `RABBIT_USER` | RabbitMQ default user |
| `RABBIT_PASS` | RabbitMQ default password |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |

### Web App

```bash
cd vertexspace-web-app
npm install
npm run dev       # Vite dev server
npm run build     # type-check and production build
npm run lint
npm run preview
```

## Author

**Aryan Bhandari**: [GitHub](https://github.com/aryanbha123)
