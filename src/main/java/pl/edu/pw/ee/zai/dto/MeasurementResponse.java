package pl.edu.pw.ee.zai.dto;

import java.time.Instant;

public class MeasurementResponse {

    private Long id;
    private Long seriesId;
    private Long sensorId;
    private Double value;
    private Instant timestamp;

    public MeasurementResponse() {
    }

    public MeasurementResponse(Long id, Long seriesId, Long sensorId, Double value, Instant timestamp) {
        this.id = id;
        this.seriesId = seriesId;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
