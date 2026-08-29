package com.mdswaley.BookingApplication.Service;

import com.mdswaley.BookingApplication.DTO.AdminReservationRequest;
import com.mdswaley.BookingApplication.DTO.ReservationRequest;
import com.mdswaley.BookingApplication.DTO.ReservationResponse;
import com.mdswaley.BookingApplication.DTO.ReservationUpdateRequest;
import com.mdswaley.BookingApplication.Entity.Reservation;
import com.mdswaley.BookingApplication.Entity.ResourceEntity;
import com.mdswaley.BookingApplication.Entity.User;
import com.mdswaley.BookingApplication.Enums.ReservationStatus;
import com.mdswaley.BookingApplication.Exception.ReservationNotFoundException;
import com.mdswaley.BookingApplication.Exception.ResourceNotFoundException;
import com.mdswaley.BookingApplication.Exception.UserNotFoundException;
import com.mdswaley.BookingApplication.Repository.ReservationRepository;
import com.mdswaley.BookingApplication.Repository.ReservationSpecification;
import com.mdswaley.BookingApplication.Repository.ResourceRepository;
import com.mdswaley.BookingApplication.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    /*
     * GET /api/reservations
     *
     * ADMIN -> sees all reservations
     * USER  -> sees only own reservations
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        User currentUser = getCurrentUser();

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        Specification<Reservation> specification =
                Specification
                        .where(
                                ReservationSpecification
                                        .hasStatus(status)
                        )
                        .and(
                                ReservationSpecification
                                        .priceGreaterThanOrEqual(minPrice)
                        )
                        .and(
                                ReservationSpecification
                                        .priceLessThanOrEqual(maxPrice)
                        );

        // USER can see only their own reservations
        if (currentUser.getRole().name().equals("USER")) {

            specification = specification.and(ReservationSpecification
                            .belongsToUser(currentUser.getId())
            );
        }

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::toResponse);
    }

    /*
     * GET /api/reservations/{id}
     */
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {

        Reservation reservation = findReservation(id);

        User currentUser = getCurrentUser();

        checkOwnershipOrAdmin(
                reservation,
                currentUser
        );

        return toResponse(reservation);
    }

    /*
     * POST /api/reservations
     *
     * USER identity comes from JWT.
     */
    public ReservationResponse createReservation(
            ReservationRequest request
    ) {

        User currentUser = getCurrentUser();

        ResourceEntity resource =
                findResource(request.resourceId());

        validateTimes(
                request.startTime(),
                request.endTime()
        );

        validateResourceAvailability(resource);

        validateNoOverlappingReservation(
                resource,
                request.startTime(),
                request.endTime()
        );

        BigDecimal price = calculatePrice(
                resource.getPrice(),
                request.startTime(),
                request.endTime()
        );

        Reservation reservation = Reservation.builder()
                .user(currentUser)
                .resource(resource)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .price(price)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved =
                reservationRepository.save(reservation);

        return toResponse(saved);
    }

    /*
     * ADMIN creates reservation for any USER.
     */
    public ReservationResponse createReservationAsAdmin(
            AdminReservationRequest request
    ) {

        User user = userRepository.findById(request.userId())
                        .orElseThrow(() ->
                                new UserNotFoundException("User not found with id: " + request.userId())
                        );

        ResourceEntity resource = findResource(request.resourceId());

        validateTimes(request.startTime(), request.endTime());

        validateResourceAvailability(resource);

        validateNoOverlappingReservation(resource, request.startTime(), request.endTime());

        /*
         * Price is calculated from the resource price.
         *
         * We don't trust the client to decide the price.
         */
        BigDecimal price = calculatePrice(
                resource.getPrice(),
                request.startTime(),
                request.endTime()
        );

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .price(price)
                .status(
                        request.status() != null
                                ? request.status()
                                : ReservationStatus.PENDING
                )
                .build();

        Reservation saved =
                reservationRepository.save(reservation);

        return toResponse(saved);
    }

    /*
     * USER / ADMIN update
     */
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {

        Reservation reservation = findReservation(id);

        User currentUser = getCurrentUser();

        checkOwnershipOrAdmin(reservation, currentUser);

        validateTimes(request.startTime(), request.endTime());

        ResourceEntity resource = reservation.getResource();

        validateNoOverlappingReservation(resource, request.startTime(), request.endTime(), reservation.getId());

        BigDecimal price = calculatePrice(resource.getPrice(), request.startTime(), request.endTime());

        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setPrice(price);

        /*
         * Don't allow USER to arbitrarily change status.
         */
        if (currentUser.getRole().name().equals("ADMIN") && request.status() != null) {
            reservation.setStatus(request.status());
        }

        return toResponse(reservation);
    }

    /*
     * DELETE /api/reservations/{id}
     */
    public void deleteReservation(Long id) {

        Reservation reservation =
                findReservation(id);

        User currentUser = getCurrentUser();

        checkOwnershipOrAdmin(
                reservation,
                currentUser
        );

        reservationRepository.delete(reservation);
    }

    /*
     * Get authenticated user from JWT/SecurityContext.
     *
     * NEVER take userId from ReservationRequest.
     */
    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {

            throw new AccessDeniedException("Authentication is required");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    private Reservation findReservation(Long id) {

        return reservationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Reservation not found with id: " + id));
    }

    private ResourceEntity findResource(Long id) {

        return resourceRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resource not found with id: " + id));
    }

    /*
     * ADMIN can access everything.
     *
     * USER can access only their own reservation.
     */
    private void checkOwnershipOrAdmin(Reservation reservation, User currentUser) {

        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        boolean isOwner = reservation.getUser()
                        .getId()
                        .equals(currentUser.getId());

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException("You do not have access to this reservation");
        }
    }

    /*
     * Validate:
     *
     * start < end
     */
    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {

        if (startTime == null || endTime == null) {

            throw new IllegalArgumentException("Start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void validateResourceAvailability(
            ResourceEntity resource
    ) {

        if (!resource.isAvailable()) {

            throw new IllegalStateException(
                    "Resource is currently unavailable"
            );
        }
    }

    /*
     * Prevent two reservations from booking
     * the same resource at overlapping times.
     */
    private void validateNoOverlappingReservation(ResourceEntity resource, LocalDateTime startTime, LocalDateTime endTime) {

        validateNoOverlappingReservation(resource, startTime, endTime, null);
    }

    private void validateNoOverlappingReservation(ResourceEntity resource, LocalDateTime startTime, LocalDateTime endTime, Long reservationId) {

        boolean overlapping = reservationRepository.existsOverlappingReservation(
                resource.getId(),
                startTime,
                endTime,
                null,
                ReservationStatus.CANCELLED
        );

        if (overlapping) {
            throw new IllegalStateException("Resource is already booked for the selected time");
        }
    }

    /*
     * PRICE CALCULATION
     *
     * Example:
     *
     * Resource price = ₹500/hour
     * Duration       = 3 hours
     *
     * Reservation price = ₹1500
     */
    private BigDecimal calculatePrice(BigDecimal hourlyPrice, LocalDateTime startTime, LocalDateTime endTime) {

        long minutes = Duration.between(startTime, endTime).toMinutes();

        if (minutes <= 0) {
            throw new IllegalArgumentException("Reservation duration must be positive");
        }

        BigDecimal hours = BigDecimal.valueOf(minutes).divide(
                                BigDecimal.valueOf(60),
                                2,
                                java.math.RoundingMode.CEILING
                        );

        return hourlyPrice
                .multiply(hours)
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                );
    }

    private ReservationResponse toResponse(
            Reservation reservation
    ) {

        return new ReservationResponse(
                reservation.getId(),

                reservation.getUser().getId(),
                reservation.getUser().getUsername(),

                reservation.getResource().getId(),
                reservation.getResource().getName(),

                reservation.getStartTime(),
                reservation.getEndTime(),

                reservation.getPrice(),
                reservation.getStatus(),

                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
