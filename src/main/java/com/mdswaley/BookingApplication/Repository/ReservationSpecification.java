package com.mdswaley.BookingApplication.Repository;

import com.mdswaley.BookingApplication.Entity.Reservation;
import com.mdswaley.BookingApplication.Enums.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Reservation> priceGreaterThanOrEqual(
            BigDecimal minPrice
    ) {
        return (root, query, cb) ->
                minPrice == null
                        ? null
                        : cb.greaterThanOrEqualTo(
                        root.get("price"),
                        minPrice
                );
    }

    public static Specification<Reservation> priceLessThanOrEqual(
            BigDecimal maxPrice
    ) {
        return (root, query, cb) ->
                maxPrice == null
                        ? null
                        : cb.lessThanOrEqualTo(
                        root.get("price"),
                        maxPrice
                );
    }

    public static Specification<Reservation> belongsToUser(
            Long userId
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("user").get("id"),
                        userId
                );
    }
}
