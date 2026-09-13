package com.arohak.hotel.repository;

import com.arohak.hotel.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByCustomerIdOrderByBookingDateDesc(Long customerId);
    List<Booking> findAllByOrderByBookingDateDesc();
    Optional<Booking> findByBookingId(String bookingId);

    @Query("""
      select count(b) from Booking b
      where b.room.id=:roomId and b.bookingStatus='CONFIRMED'
        and b.checkInDate < :checkout and b.checkOutDate > :checkin
      """)
    long countOverlaps(@Param("roomId") Long roomId,
                       @Param("checkin") LocalDate checkin,
                       @Param("checkout") LocalDate checkout);

    @Query("""
      select b from Booking b
      where b.organization.id=:orgId
      order by b.bookingDate desc
      """)
    List<Booking> findAllByOrganization(@Param("orgId") Long orgId);
}
