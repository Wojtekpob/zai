package pl.edu.pw.ee.zai.service;

import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pw.ee.zai.dto.MeasurementInput;
import pl.edu.pw.ee.zai.dto.MeasurementResponse;
import pl.edu.pw.ee.zai.exception.ResourceNotFoundException;
import pl.edu.pw.ee.zai.exception.UnauthorizedException;
import pl.edu.pw.ee.zai.exception.UnprocessableException;
import pl.edu.pw.ee.zai.model.MeasurementEntity;
import pl.edu.pw.ee.zai.model.SensorEntity;
import pl.edu.pw.ee.zai.model.SeriesEntity;
import pl.edu.pw.ee.zai.repository.MeasurementRepository;
import pl.edu.pw.ee.zai.repository.SensorRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MeasurementService {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);

    private final MeasurementRepository measurementRepository;
    private final SensorRepository sensorRepository;
    private final ApiKeyService apiKeyService;

    public MeasurementService(MeasurementRepository measurementRepository,
                              SensorRepository sensorRepository,
                              ApiKeyService apiKeyService) {
        this.measurementRepository = measurementRepository;
        this.sensorRepository = sensorRepository;
        this.apiKeyService = apiKeyService;
    }

    @Transactional
    public MeasurementResponse recordMeasurement(String rawApiKey, MeasurementInput input) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new UnauthorizedException("Missing X-API-Key header");
        }

        String hash = apiKeyService.hashApiKey(rawApiKey);
        SensorEntity sensor = sensorRepository.findByApiKeyHashAndActiveTrue(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or inactive API key"));

        SeriesEntity series = sensor.getSeries();

        Instant timestamp = input.getTimestamp() != null ? input.getTimestamp() : Instant.now();

        if (timestamp.isAfter(Instant.now().plus(Duration.ofMinutes(5)))) {
            logger.warn("Rejected measurement: timestamp {} is in the future", timestamp);
            throw new UnprocessableException("Timestamp cannot be more than 5 minutes in the future");
        }

        Double value = input.getValue();
        if (value < series.getMinValue() || value > series.getMaxValue()) {
            logger.error("Rejected measurement value {} outside series {} range [{}, {}]",
                    value, series.getId(), series.getMinValue(), series.getMaxValue());
            throw new UnprocessableException("Measurement value " + value + " outside series bounds [" +
                    series.getMinValue() + ", " + series.getMaxValue() + "]");
        }

        MeasurementEntity measurement = new MeasurementEntity(series, sensor, value, timestamp);
        MeasurementEntity saved = measurementRepository.save(measurement);

        sensor.setLastMeasurementAt(timestamp);
        sensorRepository.save(sensor);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeasurementResponse> getMeasurements(String seriesParam,
                                                     String fromParam,
                                                     String toParam,
                                                     String sortParam,
                                                     Integer limitParam) {
        final List<Long> seriesIds;
        if (seriesParam != null && !seriesParam.isBlank()) {
            try {
                seriesIds = Arrays.stream(seriesParam.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .toList();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid series parameter format: " + seriesParam);
            }
        } else {
            seriesIds = null;
        }

        final Instant fromInstant;
        if (fromParam != null && !fromParam.isBlank()) {
            try {
                fromInstant = Instant.parse(fromParam.trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid 'from' timestamp format: " + fromParam);
            }
        } else {
            fromInstant = null;
        }

        final Instant toInstant;
        if (toParam != null && !toParam.isBlank()) {
            try {
                toInstant = Instant.parse(toParam.trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid 'to' timestamp format: " + toParam);
            }
        } else {
            toInstant = null;
        }

        int limit = 1000;
        if (limitParam != null) {
            limit = Math.max(1, Math.min(10000, limitParam));
        }

        boolean isDesc = "-timestamp".equalsIgnoreCase(sortParam);
        Sort sort = isDesc ? Sort.by(Sort.Direction.DESC, "timestamp") : Sort.by(Sort.Direction.ASC, "timestamp");
        PageRequest pageRequest = PageRequest.of(0, limit, sort);

        Specification<MeasurementEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (seriesIds != null && !seriesIds.isEmpty()) {
                predicates.add(root.get("series").get("id").in(seriesIds));
            }
            if (fromInstant != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromInstant));
            }
            if (toInstant != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toInstant));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return measurementRepository.findAll(spec, pageRequest).getContent().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeasurementResponse getMeasurementById(Long id) {
        MeasurementEntity entity = measurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement not found with id: " + id));
        return mapToResponse(entity);
    }

    private MeasurementResponse mapToResponse(MeasurementEntity entity) {
        Long sensorId = entity.getSensor() != null ? entity.getSensor().getId() : null;
        return new MeasurementResponse(
                entity.getId(),
                entity.getSeries().getId(),
                sensorId,
                entity.getValue(),
                entity.getTimestamp()
        );
    }
}
