package com.arohak.hotel.controller;
import com.arohak.hotel.dto.HotelDtos.HotelRequest;
import com.arohak.hotel.entity.Hotel;
import com.arohak.hotel.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService service;
    public HotelController(HotelService service){this.service=service;}
    @GetMapping public List<Hotel> all(){return service.all();}
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public Hotel create(@Valid @RequestBody HotelRequest r){return service.create(r);}
}
