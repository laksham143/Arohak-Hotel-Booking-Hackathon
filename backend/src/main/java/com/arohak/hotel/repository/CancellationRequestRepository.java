package com.arohak.hotel.repository;
import com.arohak.hotel.entity.CancellationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CancellationRequestRepository extends JpaRepository<CancellationRequest,Long> {
    List<CancellationRequest> findAllByOrderByRequestDateDesc();
}
