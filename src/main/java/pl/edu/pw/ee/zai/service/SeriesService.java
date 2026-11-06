package pl.edu.pw.ee.zai.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pw.ee.zai.dto.SeriesInput;
import pl.edu.pw.ee.zai.dto.SeriesResponse;
import pl.edu.pw.ee.zai.exception.ConflictException;
import pl.edu.pw.ee.zai.exception.ResourceNotFoundException;
import pl.edu.pw.ee.zai.exception.UnprocessableException;
import pl.edu.pw.ee.zai.model.SeriesEntity;
import pl.edu.pw.ee.zai.repository.MeasurementRepository;
import pl.edu.pw.ee.zai.repository.SeriesRepository;

import java.time.Instant;
import java.util.List;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final MeasurementRepository measurementRepository;

    public SeriesService(SeriesRepository seriesRepository, MeasurementRepository measurementRepository) {
        this.seriesRepository = seriesRepository;
        this.measurementRepository = measurementRepository;
    }

    @Transactional(readOnly = true)
    public List<SeriesResponse> getAllSeries() {
        return seriesRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeriesResponse getSeriesById(Long id) {
        SeriesEntity entity = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with id: " + id));
        return mapToResponse(entity);
    }

    @Transactional
    public SeriesResponse createSeries(SeriesInput input) {
        if (input.getMinValue() >= input.getMaxValue()) {
            throw new UnprocessableException("minValue must be strictly less than maxValue");
        }

        SeriesEntity entity = new SeriesEntity(
                input.getName().trim(),
                input.getMinValue(),
                input.getMaxValue(),
                input.getColor(),
                input.getIcon(),
                input.getUnit(),
                Instant.now()
        );

        SeriesEntity saved = seriesRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public SeriesResponse updateSeries(Long id, SeriesInput input) {
        SeriesEntity entity = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with id: " + id));

        if (input.getMinValue() >= input.getMaxValue()) {
            throw new UnprocessableException("minValue must be strictly less than maxValue");
        }

        if (measurementRepository.existsOutOfRange(id, input.getMinValue(), input.getMaxValue())) {
            throw new ConflictException("New range excludes existing measurements for this series");
        }

        entity.setName(input.getName().trim());
        entity.setMinValue(input.getMinValue());
        entity.setMaxValue(input.getMaxValue());
        entity.setColor(input.getColor());
        entity.setIcon(input.getIcon());
        entity.setUnit(input.getUnit());

        SeriesEntity saved = seriesRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteSeries(Long id) {
        SeriesEntity entity = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with id: " + id));
        seriesRepository.delete(entity);
    }

    private SeriesResponse mapToResponse(SeriesEntity entity) {
        return new SeriesResponse(
                entity.getId(),
                entity.getName(),
                entity.getMinValue(),
                entity.getMaxValue(),
                entity.getColor(),
                entity.getIcon(),
                entity.getUnit()
        );
    }
}
