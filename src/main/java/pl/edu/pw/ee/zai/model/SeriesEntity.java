package pl.edu.pw.ee.zai.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "series")
public class SeriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "min_value", nullable = false)
    private Double minValue;

    @Column(name = "max_value", nullable = false)
    private Double maxValue;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(length = 50)
    private String icon;

    @Column(length = 20)
    private String unit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorEntity> sensors = new ArrayList<>();

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeasurementEntity> measurements = new ArrayList<>();

    public SeriesEntity() {
    }

    public SeriesEntity(String name, Double minValue, Double maxValue, String color, String icon, String unit, Instant createdAt) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.color = color;
        this.icon = icon;
        this.unit = unit;
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

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<SensorEntity> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorEntity> sensors) {
        this.sensors = sensors;
    }

    public List<MeasurementEntity> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<MeasurementEntity> measurements) {
        this.measurements = measurements;
    }
}
