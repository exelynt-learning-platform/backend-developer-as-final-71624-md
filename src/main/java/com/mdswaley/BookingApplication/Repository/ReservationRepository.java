package com.mdswaley.BookingApplication.Repository;

import com.mdswaley.BookingApplication.Entity.Reservation;
import com.mdswaley.BookingApplication.Enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.resource.id = :resourceId
          AND r.status <> :cancelledStatus
          AND r.startTime < :endTime
          AND r.endTime > :startTime
          AND (:reservationId IS NULL OR r.id <> :reservationId)
        """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("reservationId") Long reservationId,
            @Param("cancelledStatus") ReservationStatus cancelledStatus
    );
}
