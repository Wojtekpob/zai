package pl.edu.pw.ee.zai.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import pl.edu.pw.ee.zai.dto.MeasurementInput;
import pl.edu.pw.ee.zai.dto.MeasurementResponse;
import pl.edu.pw.ee.zai.service.MeasurementService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeasurementResponse> createMeasurement(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody MeasurementInput input) {
        MeasurementResponse created = measurementService.recordMeasurement(apiKey, input);
        URI location = URI.create("/api/measurements/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MeasurementResponse>> getMeasurements(
            @RequestParam(value = "series", required = false) String series,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "sort", required = false, defaultValue = "timestamp") String sort,
            @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit) {
        List<MeasurementResponse> list = measurementService.getMeasurements(series, from, to, sort, limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping(value = "/{measurementId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeasurementResponse> getMeasurementById(@PathVariable Long measurementId) {
        return ResponseEntity.ok(measurementService.getMeasurementById(measurementId));
    }

    @RequestMapping(value = "/{measurementId}", method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public void rejectModification(@PathVariable Long measurementId) throws HttpRequestMethodNotSupportedException {
        throw new HttpRequestMethodNotSupportedException("PUT/PATCH/DELETE");
    }
}
