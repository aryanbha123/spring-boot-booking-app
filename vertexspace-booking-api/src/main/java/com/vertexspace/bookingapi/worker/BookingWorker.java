package com.vertexspace.bookingapi.worker;

import com.vertexspace.bookingapi.config.RabbitMQConfig;
import com.vertexspace.bookingapi.dto.BookingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingWorker {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processBooking(BookingRequest request) {
        log.info("Received booking request from RabbitMQ: {}", request);
        try {
            // Simulate processing time
            Thread.sleep(5000);
            log.info("Successfully processed booking for user: {}", request.getUserId());
        } catch (InterruptedException e) {
            log.error("Error processing booking", e);
            Thread.currentThread().interrupt();
        }
    }
}
