package com.arohak.hotel.controller;
import com.arohak.hotel.dto.AuthDtos.*;
import com.arohak.hotel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}
    @PostMapping("/register") public AuthResponse register(@RequestBody RegisterRequest r){return service.register(r);}
    @PostMapping("/login") public AuthResponse login(@RequestBody LoginRequest r){return service.login(r);}
}
