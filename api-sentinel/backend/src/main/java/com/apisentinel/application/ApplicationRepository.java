package com.apisentinel.application;

import com.apisentinel.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByOwner(User owner);
    Page<Application> findByOwner(User owner, Pageable pageable);
    Optional<Application> findByIdAndOwner(UUID id, User owner);
}
