package com.apisentinel.usage;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.application.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    Optional<UsageRecord> findByRequestId(String requestId);

    Page<UsageRecord> findByApplicationInOrderByCreatedAtDesc(List<Application> applications, Pageable pageable);

    Page<UsageRecord> findByApiConfigurationOrderByCreatedAtDesc(ApiConfiguration apiConfiguration, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.apiConfiguration.id = :apiId AND u.createdAt >= :since")
    long countByApiIdSince(@Param("apiId") UUID apiId, @Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.apiConfiguration.id = :apiId AND u.createdAt >= :start AND u.createdAt < :end")
    long countByApiIdBetween(@Param("apiId") UUID apiId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COALESCE(SUM(u.estimatedCost), 0) FROM UsageRecord u WHERE u.apiConfiguration.id = :apiId AND u.createdAt >= :since")
    BigDecimal sumCostByApiIdSince(@Param("apiId") UUID apiId, @Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.apiConfiguration.id = :apiId AND u.statusCode >= 500 AND u.createdAt >= :since")
    long count5xxByApiIdSince(@Param("apiId") UUID apiId, @Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.application.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COALESCE(SUM(u.estimatedCost), 0) FROM UsageRecord u WHERE u.application.owner.id = :ownerId")
    BigDecimal sumCostByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.application.owner.id = :ownerId AND (u.statusCode >= 400 OR u.rejected = true)")
    long countErrorsByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.application.owner.id = :ownerId AND u.cacheHit = true")
    long countCacheHitsByOwnerId(@Param("ownerId") UUID ownerId);
}
