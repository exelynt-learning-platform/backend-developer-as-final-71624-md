package com.mdswaley.BookingApplication.DTO;

public record LoginResponse(
        String token,
        String type
) {
}
