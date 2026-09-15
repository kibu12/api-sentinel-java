package com.apisentinel.api;

import com.apisentinel.application.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiConfigurationRepository extends JpaRepository<ApiConfiguration, UUID> {
    List<ApiConfiguration> findByApplication(Application application);
    List<ApiConfiguration> findByApplicationIn(List<Application> applications);
    Page<ApiConfiguration> findByApplicationIn(List<Application> applications, Pageable pageable);

    @Query("SELECT a FROM ApiConfiguration a WHERE a.id = :id AND a.application.owner.id = :ownerId")
    Optional<ApiConfiguration> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
}
