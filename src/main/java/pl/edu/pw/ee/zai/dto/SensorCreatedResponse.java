package pl.edu.pw.ee.zai.dto;

import java.time.Instant;

public class SensorCreatedResponse extends SensorResponse {

    private String apiKey;

    public SensorCreatedResponse() {
    }

    public SensorCreatedResponse(Long id, String name, Long seriesId, Instant createdAt, Instant lastMeasurementAt, String apiKey) {
        super(id, name, seriesId, createdAt, lastMeasurementAt);
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
