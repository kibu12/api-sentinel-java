package com.apisentinel.apikey;

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
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByKeyHash(String keyHash);
    List<ApiKey> findByApplication(Application application);
    List<ApiKey> findByApplicationIn(List<Application> applications);
    Page<ApiKey> findByApplicationIn(List<Application> applications, Pageable pageable);

    @Query("SELECT k FROM ApiKey k WHERE k.id = :id AND k.application.owner.id = :ownerId")
    Optional<ApiKey> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
}
