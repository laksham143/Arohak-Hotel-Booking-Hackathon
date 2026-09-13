package com.arohak.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arohak.hotel.dto.BookingDtos.*;
import com.arohak.hotel.dto.BookingDtos.CancelRequest;
import com.arohak.hotel.dto.BookingDtos.CreateBookingRequest;
import com.arohak.hotel.entity.Booking;
import com.arohak.hotel.entity.CancellationRequest;
import com.arohak.hotel.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service){this.service=service;}

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Booking create(@Valid @RequestBody CreateBookingRequest r,Authentication a){
        return service.create(r,a.getName());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<Booking> mine(Authentication a){return service.myBookings(a.getName());}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<Booking> all(Authentication a){return service.allBookings(a.getName());}

    @PostMapping("/{bookingId}/cancel")
    public Map<String,String> cancel(@PathVariable String bookingId,
            @RequestBody(required=false) CancelRequest r,Authentication a){
        String result=service.cancel(bookingId,a.getName(),r==null?"":r.reason());
        return Map.of("message",result);
    }

    @GetMapping("/cancellations")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<CancellationRequest> cancellations(Authentication a){
        return service.cancellationRequests(a.getName());
    }

    @PostMapping("/cancellations/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public Map<String,String> review(@PathVariable Long id,@RequestParam boolean approve,Authentication a){
        return Map.of("message",service.reviewCancellation(id,approve,a.getName()));
    }
}
