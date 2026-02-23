# VertexSpace Booking API (Spring Boot)

A Java-based backend for high-performance booking services, featuring asynchronous job processing with RabbitMQ, rate limiting via Bucket4j, and Redis integration.

## 🚀 Quick Start (Local)

### Option 1: Full Stack (Docker)
```bash
docker-compose up --build
```

### Option 2: Infrastructure in Docker, App Locally
1. **Start RabbitMQ & Redis:**
   ```bash
   docker-compose up -d rabbitmq redis
   ```
2. **Run Application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## 🛠️ How to Extend

### 1. Creating a New Controller & Route
Controllers should be placed in `src/main/java/com/vertexspace/bookingapi/controller/`.

```java
@RestController
@RequestMapping("/api/v1/new-feature")
public class NewFeatureController {

    @GetMapping("/{id}")
    public ResponseEntity<String> getFeature(@PathVariable String id) {
        return ResponseEntity.ok("Feature " + id);
    }
}
```

### 2. Adding a DTO (Data Transfer Object)
Define request/response bodies in `src/main/java/com/vertexspace/bookingapi/dto/`. Use `@Data` for boilerplate-free getters/setters.

```java
@Data
public class MyRequestDTO implements Serializable {
    private String name;
    private int value;
}
```

### 3. Using Rate Limiting
To rate-limit a route, inject `RateLimitingService` and check the bucket:

```java
@Autowired
private RateLimitingService rateLimitingService;

@PostMapping
public ResponseEntity<String> limitedRoute(@RequestBody MyRequestDTO request) {
    Bucket bucket = rateLimitingService.resolveBucket(request.getUserId());
    if (bucket.tryConsume(1)) {
        return ResponseEntity.ok("Success!");
    }
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Try again later");
}
```

### 4. Sending Asynchronous Jobs (RabbitMQ)
Inject `RabbitTemplate` to send messages to the queue:

```java
@Autowired
private RabbitTemplate rabbitTemplate;

public void processLater(MyRequestDTO data) {
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.EXCHANGE_NAME, 
        RabbitMQConfig.ROUTING_KEY, 
        data
    );
}
```

### 5. Consuming Jobs (Worker)
Add a method with `@RabbitListener` in `src/main/java/com/vertexspace/bookingapi/worker/`:

```java
@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
public void handleJob(MyRequestDTO data) {
    // Heavy processing here...
}
```

## 📡 API Endpoints
- **Health Check**: `GET /api/v1/health`
- **Create Booking**: `POST /api/v1/booking` (Rate limited to 5 req/min)
- **RabbitMQ Dashboard**: `http://localhost:15672` (guest/guest)
