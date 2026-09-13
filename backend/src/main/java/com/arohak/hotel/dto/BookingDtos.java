package com.arohak.hotel.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public class BookingDtos {
    public record SearchRequest(@NotNull LocalDate checkIn,@NotNull LocalDate checkOut,@Min(1) int guests,Long hotelId) {}
    public record CreateBookingRequest(@NotNull Long roomId,@NotNull LocalDate checkIn,@NotNull LocalDate checkOut,@Min(1) int guests) {}
    public record CancelRequest(String reason) {}
}
