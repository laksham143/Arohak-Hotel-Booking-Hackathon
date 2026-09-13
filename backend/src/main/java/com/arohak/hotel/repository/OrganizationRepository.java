package com.arohak.hotel.repository;
import com.arohak.hotel.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OrganizationRepository extends JpaRepository<Organization,Long> {
    Optional<Organization> findByName(String name);
}
