package com.arohak.hotel.service;
import com.arohak.hotel.dto.HotelDtos.HotelRequest;
import com.arohak.hotel.entity.*;
import com.arohak.hotel.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class HotelService {
    private final HotelRepository hotels; private final OrganizationRepository orgs;
    public HotelService(HotelRepository hotels,OrganizationRepository orgs){this.hotels=hotels;this.orgs=orgs;}
    public List<Hotel> all(){return hotels.findAll();}
    public Hotel create(HotelRequest r){
        Organization o=orgs.findById(r.organizationId()).orElseThrow();
        Hotel h=new Hotel(null,r.hotelId(),r.name(),r.address(),r.city(),r.description(),r.contactNumber(),
                r.email(),r.status()==null?"ACTIVE":r.status(),o); return hotels.save(h);
    }
}
