package com.tspevo.repository;

import com.tspevo.model.DistanceMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistanceMatrixRepository extends JpaRepository<DistanceMatrix, Long> {
    
    Optional<DistanceMatrix> findBySourceCityIdAndTargetCityId(Long sourceCityId, Long targetCityId);
    
    List<DistanceMatrix> findBySourceCityId(Long sourceCityId);
    
    void deleteAllBySourceCityId(Long sourceCityId);
}
