package com.mdswaley.BookingApplication.DTO;

import com.mdswaley.BookingApplication.Enums.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AdminReservationRequest(

        @NotNull(message = "User ID is required")
        @Positive
        Long userId,

        @NotNull(message = "Resource ID is required")
        @Positive
        Long resourceId,

        @NotNull(message = "Start time is required")
        @Future
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @Future
        LocalDateTime endTime,

        @NotNull(message = "Status is required")
        ReservationStatus status

) {
}
