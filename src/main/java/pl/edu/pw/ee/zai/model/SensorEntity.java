package pl.edu.pw.ee.zai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sensors")
public class SensorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private SeriesEntity series;

    @Column(name = "api_key_hash", nullable = false, unique = true, length = 64)
    private String apiKeyHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_measurement_at")
    private Instant lastMeasurementAt;

    public SensorEntity() {
    }

    public SensorEntity(String name, SeriesEntity series, String apiKeyHash, Instant createdAt) {
        this.name = name;
        this.series = series;
        this.apiKeyHash = apiKeyHash;
        this.active = true;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SeriesEntity getSeries() {
        return series;
    }

    public void setSeries(SeriesEntity series) {
        this.series = series;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastMeasurementAt() {
        return lastMeasurementAt;
    }

    public void setLastMeasurementAt(Instant lastMeasurementAt) {
        this.lastMeasurementAt = lastMeasurementAt;
    }
}
