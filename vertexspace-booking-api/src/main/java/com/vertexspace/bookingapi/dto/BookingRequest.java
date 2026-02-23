package com.vertexspace.bookingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest implements Serializable {
    private String userId;
    private String itemId;
    private int quantity;
}
