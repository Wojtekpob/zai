package pl.edu.pw.ee.zai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "measurements")
public class MeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private SeriesEntity series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private SensorEntity sensor;

    @Column(name = "val", nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;

    public MeasurementEntity() {
    }

    public MeasurementEntity(SeriesEntity series, SensorEntity sensor, Double value, Instant timestamp) {
        this.series = series;
        this.sensor = sensor;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SeriesEntity getSeries() {
        return series;
    }

    public void setSeries(SeriesEntity series) {
        this.series = series;
    }

    public SensorEntity getSensor() {
        return sensor;
    }

    public void setSensor(SensorEntity sensor) {
        this.sensor = sensor;
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
