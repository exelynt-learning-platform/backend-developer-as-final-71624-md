package com.mdswaley.BookingApplication.DTO;


import com.mdswaley.BookingApplication.Enums.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationUpdateRequest(

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        @NotNull(message = "Status is required")
        ReservationStatus status

) {
}
