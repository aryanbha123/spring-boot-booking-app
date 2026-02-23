package com.vertexspace.bookingapi.controller;

import com.vertexspace.bookingapi.config.RabbitMQConfig;
import com.vertexspace.bookingapi.dto.BookingRequest;
import com.vertexspace.bookingapi.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final RabbitTemplate rabbitTemplate;
    private final RateLimitingService rateLimitingService;

    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest request) {
        String apiKey = request.getUserId();
        Bucket bucket = rateLimitingService.resolveBucket(apiKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.info("Enqueuing booking request for user: {}", request.getUserId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, request);
            return ResponseEntity.ok("Booking request queued for processing. Remaining tokens: " + probe.getRemainingTokens());
        }

        long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
        log.warn("Rate limit exceeded for user: {}", request.getUserId());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again in " + waitForRefill + " seconds.");
    }
}
