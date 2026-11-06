package pl.edu.pw.ee.zai.dto;

import java.time.Instant;

public class SensorResponse {

    private Long id;
    private String name;
    private Long seriesId;
    private Instant createdAt;
    private Instant lastMeasurementAt;

    public SensorResponse() {
    }

    public SensorResponse(Long id, String name, Long seriesId, Instant createdAt, Instant lastMeasurementAt) {
        this.id = id;
        this.name = name;
        this.seriesId = seriesId;
        this.createdAt = createdAt;
        this.lastMeasurementAt = lastMeasurementAt;
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

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
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
