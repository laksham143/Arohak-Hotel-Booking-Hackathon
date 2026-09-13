package com.arohak.hotel.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.arohak.hotel.entity.Hotel;
import com.arohak.hotel.entity.Organization;
import com.arohak.hotel.entity.Role;
import com.arohak.hotel.entity.Room;
import com.arohak.hotel.entity.User;
import com.arohak.hotel.repository.HotelRepository;
import com.arohak.hotel.repository.OrganizationRepository;
import com.arohak.hotel.repository.RoomRepository;
import com.arohak.hotel.repository.UserRepository;

@Configuration
public class DataInitializer {
    @Bean CommandLineRunner seed(OrganizationRepository orgs,UserRepository users,HotelRepository hotels,
                                 RoomRepository rooms,PasswordEncoder encoder){
        return args -> {
            Organization org=orgs.findByName("AROHAK Demo Organization").orElseGet(
                ()->orgs.save(new Organization(null,"AROHAK Demo Organization","ACTIVE")));

            if(users.findByEmail("admin@arohak.com").isEmpty())
                users.save(new User(null,"System Admin","admin@arohak.com",encoder.encode("Admin@123"),
                        Role.ADMIN,org));
            if(users.findByEmail("reception@arohak.com").isEmpty())
                users.save(new User(null,"Front Desk","reception@arohak.com",encoder.encode("Reception@123"),
                        Role.RECEPTIONIST,org));
            if(users.findByEmail("customer@arohak.com").isEmpty())
                users.save(new User(null,"Demo Customer","customer@arohak.com",encoder.encode("Customer@123"),
                        Role.CUSTOMER,org));

            Hotel hotel=hotels.findByHotelId("HGMUM001").orElseGet(()->hotels.save(
                new Hotel(null,"HGMUM001","The Meridian Grand Mumbai",
                    "18 Marine View Road, Nariman Point, Mumbai, Maharashtra 400021","Mumbai",
                    "Fictional hotel dataset for AROHAK Hackathon","+91 22 4567 8900",
                    "reservations@meridiangrand.example","ACTIVE",org)));

            if(rooms.findByHotelId(hotel.getId()).isEmpty()){
                rooms.save(new Room(null,"R-DK-101","101","Deluxe King",2,new BigDecimal("8500"),
                    "AVAILABLE","King bed, city view, work desk","Wi-Fi, Air conditioning, Smart TV, Safe, Hair dryer, Mini refrigerator",hotel));
                rooms.save(new Room(null,"R-DT-102","102","Deluxe Twin",2,new BigDecimal("8500"),
                    "AVAILABLE","Two twin beds, city view, work desk","Wi-Fi, Air conditioning, Smart TV, Safe, Hair dryer, Mini refrigerator",hotel));
                rooms.save(new Room(null,"R-PS-201","201","Premier Sea View",3,new BigDecimal("12500"),
                    "AVAILABLE","King bed, sea view, sofa chair","Wi-Fi, Air conditioning, Smart TV, Safe, Hair dryer, Mini refrigerator",hotel));
                rooms.save(new Room(null,"R-ES-301","301","Executive Suite",3,new BigDecimal("18000"),
                    "AVAILABLE","Bedroom, living area, sea view","Wi-Fi, Air conditioning, Smart TV, Safe, Bathrobe, Premium bathroom amenities",hotel));
                rooms.save(new Room(null,"R-FS-401","401","Family Suite",4,new BigDecimal("22000"),
                    "AVAILABLE","Two bedrooms, living area, dining table","Wi-Fi, Air conditioning, Smart TV, Safe, Bathrobe, Premium bathroom amenities",hotel));
            }
        };
    }
}
