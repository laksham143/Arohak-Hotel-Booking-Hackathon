package com.arohak.hotel.service;

import com.arohak.hotel.dto.RoomDtos.RoomRequest;
import com.arohak.hotel.entity.*;
import com.arohak.hotel.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RoomService {
    private final RoomRepository rooms; private final HotelRepository hotels;
    public RoomService(RoomRepository rooms,HotelRepository hotels){this.rooms=rooms;this.hotels=hotels;}
    public List<Room> all(){return rooms.findAll();}
    public List<Room> byHotel(Long hotelId){return rooms.findByHotelId(hotelId);}
    public Room create(RoomRequest r){
        Hotel h=hotels.findById(r.hotelId()).orElseThrow();
        Room room=new Room(null,r.roomId(),r.roomNumber(),r.roomType(),r.capacity(),r.pricePerNight(),
                r.availabilityStatus()==null?"AVAILABLE":r.availabilityStatus(),r.description(),r.amenities(),h);
        return rooms.save(room);
    }
    public Room update(Long id,RoomRequest r){
        Room room=rooms.findById(id).orElseThrow(); Hotel h=hotels.findById(r.hotelId()).orElseThrow();
        room.setRoomId(r.roomId());room.setRoomNumber(r.roomNumber());room.setRoomType(r.roomType());
        room.setCapacity(r.capacity());room.setPricePerNight(r.pricePerNight());
        room.setAvailabilityStatus(r.availabilityStatus());room.setDescription(r.description());
        room.setAmenities(r.amenities());room.setHotel(h); return rooms.save(room);
    }
}
