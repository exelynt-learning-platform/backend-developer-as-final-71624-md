package com.mdswaley.BookingApplication.DTO;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ResourceRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotBlank(message = "Type is required")
        String type,

        @NotNull(message = "Availability is required")
        Boolean available,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true,
                message = "Price cannot be negative")
        @Digits(integer = 10, fraction = 2,
                message = "Price must have at most 2 decimal places")
        BigDecimal price

) {
}
