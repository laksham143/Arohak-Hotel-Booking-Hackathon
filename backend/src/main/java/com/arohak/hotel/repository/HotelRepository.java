package com.arohak.hotel.repository;
import com.arohak.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface HotelRepository extends JpaRepository<Hotel,Long> {
    Optional<Hotel> findByHotelId(String hotelId);
    List<Hotel> findByOrganizationId(Long organizationId);
}
