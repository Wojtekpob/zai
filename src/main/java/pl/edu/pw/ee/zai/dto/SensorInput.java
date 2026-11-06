package pl.edu.pw.ee.zai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SensorInput {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    private Long seriesId;

    public SensorInput() {
    }

    public SensorInput(String name, Long seriesId) {
        this.name = name;
        this.seriesId = seriesId;
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
}
