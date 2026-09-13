package com.arohak.hotel.dto;
import jakarta.validation.constraints.*;
public class HotelDtos {
    public record HotelRequest(@NotBlank String hotelId,@NotBlank String name,@NotBlank String address,
                               @NotBlank String city,String description,String contactNumber,String email,
                               String status,Long organizationId) {}
}
