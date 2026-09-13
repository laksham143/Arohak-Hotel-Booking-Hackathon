package com.arohak.hotel.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class RoomDtos {
    public record RoomRequest(@NotBlank String roomId,@NotBlank String roomNumber,@NotBlank String roomType,
                              @Min(1) int capacity,@Positive BigDecimal pricePerNight,
                              String availabilityStatus,String description,String amenities,Long hotelId) {}
}
