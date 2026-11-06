package pl.edu.pw.ee.zai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SeriesInput {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    private Double minValue;

    @NotNull
    private Double maxValue;

    @NotNull
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String color;

    private String icon;

    @Size(max = 20)
    private String unit;

    public SeriesInput() {
    }

    public SeriesInput(String name, Double minValue, Double maxValue, String color, String icon, String unit) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.color = color;
        this.icon = icon;
        this.unit = unit;
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
}
