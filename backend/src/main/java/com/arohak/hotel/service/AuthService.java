package com.arohak.hotel.service;

import com.arohak.hotel.dto.AuthDtos.*;
import com.arohak.hotel.entity.*;
import com.arohak.hotel.repository.*;
import com.arohak.hotel.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final OrganizationRepository orgs;
    private final PasswordEncoder encoder;
    private final AuthenticationManager auth;
    private final JwtService jwt;

    public AuthService(UserRepository users, OrganizationRepository orgs,
                       PasswordEncoder encoder, AuthenticationManager auth, JwtService jwt) {
        this.users=users; this.orgs=orgs; this.encoder=encoder; this.auth=auth; this.jwt=jwt;
    }

    public AuthResponse register(RegisterRequest r) {
        if (users.findByEmail(r.email()).isPresent())
            throw new RuntimeException("Email already registered");

        // Public self-registration creates a CUSTOMER.
        // Admin/Receptionist accounts are created by the application administrator.
        Organization org = r.organizationId() == null
                ? orgs.findByName("AROHAK Demo Organization").orElseThrow()
                : orgs.findById(r.organizationId()).orElseThrow();

        User u = new User(null, r.name(), r.email(), encoder.encode(r.password()),
                Role.CUSTOMER, org);
        u = users.save(u);

        return response(u);
    }

    public AuthResponse login(LoginRequest r) {
        auth.authenticate(new UsernamePasswordAuthenticationToken(r.email(), r.password()));
        return response(users.findByEmail(r.email()).orElseThrow());
    }

    private AuthResponse response(User u) {
        Long orgId = u.getOrganization()==null ? null : u.getOrganization().getId();
        return new AuthResponse(
                jwt.generate(u.getId(),u.getEmail(),u.getRole().name(),orgId),
                u.getId(),u.getName(),u.getEmail(),u.getRole().name(),orgId);
    }
}
