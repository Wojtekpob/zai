package pl.edu.pw.ee.zai.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class MeasurementInput {

    @NotNull
    private Double value;

    private Instant timestamp;

    public MeasurementInput() {
    }

    public MeasurementInput(Double value, Instant timestamp) {
        this.value = value;
        this.timestamp = timestamp;
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
