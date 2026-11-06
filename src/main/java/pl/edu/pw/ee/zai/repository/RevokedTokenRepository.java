package pl.edu.pw.ee.zai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pw.ee.zai.model.RevokedTokenEntity;

import java.time.Instant;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedTokenEntity, String> {
    boolean existsByTokenId(String tokenId);
    void deleteByExpiryDateBefore(Instant now);
}
