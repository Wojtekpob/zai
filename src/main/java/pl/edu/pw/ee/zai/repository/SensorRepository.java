package pl.edu.pw.ee.zai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pw.ee.zai.model.SensorEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<SensorEntity, Long> {
    Optional<SensorEntity> findByApiKeyHashAndActiveTrue(String apiKeyHash);
    List<SensorEntity> findAllBySeriesId(Long seriesId);
}
