package com.arohak.hotel.controller;
import com.arohak.hotel.dto.RoomDtos.RoomRequest;
import com.arohak.hotel.dto.BookingDtos.SearchRequest;
import com.arohak.hotel.entity.Room;
import com.arohak.hotel.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
@RestController @RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService rooms; private final BookingService bookings;
    public RoomController(RoomService rooms,BookingService bookings){this.rooms=rooms;this.bookings=bookings;}
    @GetMapping public List<Room> all(){return rooms.all();}
    @GetMapping("/hotel/{hotelId}") public List<Room> byHotel(@PathVariable Long hotelId){return rooms.byHotel(hotelId);}
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')") public Room create(@Valid @RequestBody RoomRequest r){return rooms.create(r);}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')") public Room update(@PathVariable Long id,@Valid @RequestBody RoomRequest r){return rooms.update(id,r);}
    @PostMapping("/search") public List<Room> search(@Valid @RequestBody SearchRequest r){return bookings.search(r);}
}
