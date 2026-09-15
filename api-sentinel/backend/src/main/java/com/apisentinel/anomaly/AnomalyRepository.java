package com.apisentinel.anomaly;

import com.apisentinel.api.ApiConfiguration;
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
public interface AnomalyRepository extends JpaRepository<Anomaly, UUID> {

    List<Anomaly> findByApiConfigurationAndStatus(ApiConfiguration apiConfiguration, String status);

    Optional<Anomaly> findFirstByApiConfigurationAndTypeAndStatus(ApiConfiguration apiConfiguration, String type, String status);

    Page<Anomaly> findByApiConfigurationInOrderByDetectedAtDesc(List<ApiConfiguration> apiConfigurations, Pageable pageable);

    @Query("SELECT a FROM Anomaly a WHERE a.apiConfiguration.application.owner.id = :ownerId ORDER BY a.detectedAt DESC")
    Page<Anomaly> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Anomaly a WHERE a.apiConfiguration.application.owner.id = :ownerId AND a.status = 'OPEN'")
    long countOpenByOwnerId(@Param("ownerId") UUID ownerId);
}
