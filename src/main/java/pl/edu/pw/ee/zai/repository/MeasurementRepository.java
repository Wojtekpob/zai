package pl.edu.pw.ee.zai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.pw.ee.zai.model.MeasurementEntity;

@Repository
public interface MeasurementRepository extends JpaRepository<MeasurementEntity, Long>, JpaSpecificationExecutor<MeasurementEntity> {

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MeasurementEntity m " +
           "WHERE m.series.id = :seriesId AND (m.value < :minValue OR m.value > :maxValue)")
    boolean existsOutOfRange(@Param("seriesId") Long seriesId,
                             @Param("minValue") Double minValue,
                             @Param("maxValue") Double maxValue);

    long countBySeriesId(Long seriesId);
}
