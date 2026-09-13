package com.arohak.hotel.repository;

import com.arohak.hotel.entity.Room;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findByHotelId(Long hotelId);

    @Query("""
      select r from Room r
      where r.hotel.id=:hotelId
        and r.availabilityStatus='AVAILABLE'
        and r.capacity >= :guests
        and not exists (
          select b.id from Booking b
          where b.room.id=r.id
            and b.bookingStatus='CONFIRMED'
            and b.checkInDate < :checkout
            and b.checkOutDate > :checkin
        )
      """)
    List<Room> findAvailable(@Param("hotelId") Long hotelId,
                             @Param("checkin") LocalDate checkin,
                             @Param("checkout") LocalDate checkout,
                             @Param("guests") Integer guests);
}
