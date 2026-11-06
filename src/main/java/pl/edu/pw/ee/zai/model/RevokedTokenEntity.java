package pl.edu.pw.ee.zai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens")
public class RevokedTokenEntity {

    @Id
    @Column(name = "token_id", length = 255)
    private String tokenId;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    public RevokedTokenEntity() {
    }

    public RevokedTokenEntity(String tokenId, Instant expiryDate) {
        this.tokenId = tokenId;
        this.expiryDate = expiryDate;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
}
