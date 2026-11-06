package pl.edu.pw.ee.zai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pw.ee.zai.model.SeriesEntity;

@Repository
public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {
}
