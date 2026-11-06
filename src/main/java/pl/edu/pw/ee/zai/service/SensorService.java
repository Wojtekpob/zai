package pl.edu.pw.ee.zai.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pw.ee.zai.dto.SensorCreatedResponse;
import pl.edu.pw.ee.zai.dto.SensorInput;
import pl.edu.pw.ee.zai.dto.SensorResponse;
import pl.edu.pw.ee.zai.exception.ResourceNotFoundException;
import pl.edu.pw.ee.zai.exception.UnprocessableException;
import pl.edu.pw.ee.zai.model.SensorEntity;
import pl.edu.pw.ee.zai.model.SeriesEntity;
import pl.edu.pw.ee.zai.repository.SensorRepository;
import pl.edu.pw.ee.zai.repository.SeriesRepository;

import java.time.Instant;
import java.util.List;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final SeriesRepository seriesRepository;
    private final ApiKeyService apiKeyService;

    public SensorService(SensorRepository sensorRepository,
                         SeriesRepository seriesRepository,
                         ApiKeyService apiKeyService) {
        this.sensorRepository = sensorRepository;
        this.seriesRepository = seriesRepository;
        this.apiKeyService = apiKeyService;
    }

    @Transactional(readOnly = true)
    public List<SensorResponse> getAllSensors() {
        return sensorRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SensorResponse getSensorById(Long id) {
        SensorEntity entity = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
        return mapToResponse(entity);
    }

    @Transactional
    public SensorCreatedResponse createSensor(SensorInput input) {
        SeriesEntity series = seriesRepository.findById(input.getSeriesId())
                .orElseThrow(() -> new UnprocessableException("Series not found with id: " + input.getSeriesId()));

        String rawApiKey = apiKeyService.generateRawApiKey();
        String hash = apiKeyService.hashApiKey(rawApiKey);

        SensorEntity entity = new SensorEntity(
                input.getName().trim(),
                series,
                hash,
                Instant.now()
        );

        SensorEntity saved = sensorRepository.save(entity);

        return new SensorCreatedResponse(
                saved.getId(),
                saved.getName(),
                saved.getSeries().getId(),
                saved.getCreatedAt(),
                saved.getLastMeasurementAt(),
                rawApiKey
        );
    }

    @Transactional
    public void deleteSensor(Long id) {
        SensorEntity entity = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
        sensorRepository.delete(entity);
    }

    private SensorResponse mapToResponse(SensorEntity entity) {
        return new SensorResponse(
                entity.getId(),
                entity.getName(),
                entity.getSeries().getId(),
                entity.getCreatedAt(),
                entity.getLastMeasurementAt()
        );
    }
}
