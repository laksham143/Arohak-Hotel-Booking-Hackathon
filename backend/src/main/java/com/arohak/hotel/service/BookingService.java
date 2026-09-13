package com.arohak.hotel.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arohak.hotel.dto.BookingDtos.CreateBookingRequest;
import com.arohak.hotel.dto.BookingDtos.SearchRequest;
import com.arohak.hotel.entity.Booking;
import com.arohak.hotel.entity.BookingStatus;
import com.arohak.hotel.entity.CancellationRequest;
import com.arohak.hotel.entity.RequestStatus;
import com.arohak.hotel.entity.Role;
import com.arohak.hotel.entity.Room;
import com.arohak.hotel.entity.User;
import com.arohak.hotel.repository.BookingRepository;
import com.arohak.hotel.repository.CancellationRequestRepository;
import com.arohak.hotel.repository.RoomRepository;
import com.arohak.hotel.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final UserRepository users;
    private final CancellationRequestRepository cancellations;

    public BookingService(BookingRepository bookings,
                           RoomRepository rooms,
                           UserRepository users,
                           CancellationRequestRepository cancellations) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.users = users;
        this.cancellations = cancellations;
    }

    public List<Room> search(SearchRequest r) {
        validateDates(r.checkIn(), r.checkOut());

        if (r.hotelId() != null) {
            return rooms.findAvailable(
                    r.hotelId(),
                    r.checkIn(),
                    r.checkOut(),
                    r.guests()
            );
        }

        List<Room> result = new ArrayList<>();

        rooms.findAll().forEach(room -> {
            if ("AVAILABLE".equals(room.getAvailabilityStatus())
                    && room.getCapacity() >= r.guests()
                    && bookings.countOverlaps(
                            room.getId(),
                            r.checkIn(),
                            r.checkOut()
                    ) == 0) {

                result.add(room);
            }
        });

        return result;
    }

    @Transactional
    public Booking create(CreateBookingRequest r, String email) {

        User customer = users.findByEmail(email).orElseThrow();

        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customers can create bookings"
            );
        }

        validateDates(r.checkIn(), r.checkOut());

        Room room = rooms.findById(r.roomId()).orElseThrow();

        if (!"AVAILABLE".equals(room.getAvailabilityStatus())) {
            throw new IllegalStateException(
                    "Room is inactive/unavailable"
            );
        }

        if (r.guests() > room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Guest count exceeds room capacity"
            );
        }

        if (bookings.countOverlaps(
                room.getId(),
                r.checkIn(),
                r.checkOut()
        ) > 0) {

            throw new IllegalStateException(
                    "Room is no longer available for these dates"
            );
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                r.checkIn(),
                r.checkOut()
        );

        BigDecimal total = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        String bid = "BK-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        Booking b = new Booking(
                null,
                bid,
                customer,
                customer.getOrganization(),
                room.getHotel(),
                room,
                r.checkIn(),
                r.checkOut(),
                r.guests(),
                LocalDateTime.now(),
                total,
                BookingStatus.CONFIRMED
        );

        return bookings.save(b);
    }

    public List<Booking> myBookings(String email) {

        User u = users.findByEmail(email).orElseThrow();

        return bookings.findByCustomerIdOrderByBookingDateDesc(
                u.getId()
        );
    }

    public List<Booking> allBookings(String email) {

        User staff = users.findByEmail(email).orElseThrow();

        if (staff.getRole() == Role.CUSTOMER) {
            throw new SecurityException("Staff access required");
        }

        return staff.getOrganization() == null
                ? bookings.findAllByOrderByBookingDateDesc()
                : bookings.findAllByOrganization(
                        staff.getOrganization().getId()
                );
    }

    @Transactional
    public String cancel(
            String bookingId,
            String email,
            String reason) {

        Booking b = bookings.findByBookingId(bookingId).orElseThrow();
        User u = users.findByEmail(email).orElseThrow();

        boolean staff =
                u.getRole() == Role.ADMIN ||
                u.getRole() == Role.RECEPTIONIST;

        if (!staff && !b.getCustomer().getId().equals(u.getId())) {
            throw new SecurityException("Not authorized");
        }

        if (b.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking already cancelled"
            );
        }

        if (staff) {

            ensureSameOrganization(u, b);

            b.setBookingStatus(BookingStatus.CANCELLED);

            bookings.save(b);

            return "Booking cancelled successfully";
        }

        // Direct cancellation is allowed only until
        // 24 hours before check-in.
        long hours = Duration.between(
                LocalDateTime.now(),
                b.getCheckInDate().atStartOfDay()
        ).toHours();

        if (hours >= 24) {

            b.setBookingStatus(BookingStatus.CANCELLED);

            bookings.save(b);

            return "Booking cancelled successfully";
        }

        // Less than 24 hours before check-in:
        // create a cancellation request for staff review.
        cancellations.save(
                new CancellationRequest(
                        null,
                        b,
                        u,
                        reason,
                        LocalDateTime.now(),
                        RequestStatus.PENDING
                )
        );

        return "Cancellation request submitted for staff review";
    }

    @Transactional
    public String reviewCancellation(
            Long requestId,
            boolean approve,
            String email) {

        User staff = users.findByEmail(email).orElseThrow();

        if (staff.getRole() == Role.CUSTOMER) {
            throw new SecurityException(
                    "Staff access required"
            );
        }

        CancellationRequest r =
                cancellations.findById(requestId).orElseThrow();

        ensureSameOrganization(
                staff,
                r.getBooking()
        );

        if (r.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Request already reviewed"
            );
        }

        r.setStatus(
                approve
                        ? RequestStatus.APPROVED
                        : RequestStatus.REJECTED
        );

        if (approve) {

            r.getBooking().setBookingStatus(
                    BookingStatus.CANCELLED
            );

            bookings.save(r.getBooking());
        }

        cancellations.save(r);

        return approve
                ? "Cancellation approved"
                : "Cancellation rejected";
    }

    /*
     * IMPORTANT FIX:
     * Keep the Hibernate session open while accessing
     * r.getBooking().getOrganization().
     */
    @Transactional(readOnly = true)
    public List<CancellationRequest> cancellationRequests(
            String email) {

        User staff = users.findByEmail(email).orElseThrow();

        if (staff.getRole() == Role.CUSTOMER) {
            throw new SecurityException(
                    "Staff access required"
            );
        }

        return cancellations
                .findAllByOrderByRequestDateDesc()
                .stream()
                .filter(r ->
                        staff.getOrganization() == null
                                || r.getBooking()
                                   .getOrganization()
                                   .getId()
                                   .equals(
                                        staff.getOrganization().getId()
                                   )
                )
                .toList();
    }

    private void validateDates(
            LocalDate in,
            LocalDate out) {

        if (!out.isAfter(in)) {
            throw new IllegalArgumentException(
                    "Check-out must be after check-in"
            );
        }

        if (in.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Check-in cannot be in the past"
            );
        }
    }

    private void ensureSameOrganization(
            User staff,
            Booking booking) {

        if (staff.getOrganization() != null
                && booking.getOrganization() != null
                && !staff.getOrganization()
                        .getId()
                        .equals(
                            booking.getOrganization().getId()
                        )) {

            throw new SecurityException(
                    "Organization access denied"
            );
        }
    }
}