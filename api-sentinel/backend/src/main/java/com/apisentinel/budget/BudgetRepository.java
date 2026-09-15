package com.apisentinel.budget;

import com.apisentinel.api.ApiConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByApiConfiguration(ApiConfiguration apiConfiguration);
    Optional<Budget> findByApiConfigurationAndPeriodType(ApiConfiguration apiConfiguration, String periodType);
}
