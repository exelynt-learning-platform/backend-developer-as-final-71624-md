package com.mdswaley.BookingApplication.DTO;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResourceResponse(

        Long id,
        String name,
        String description,
        String type,
        boolean available,
        BigDecimal price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
