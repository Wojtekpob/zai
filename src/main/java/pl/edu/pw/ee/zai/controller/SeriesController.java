package pl.edu.pw.ee.zai.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pw.ee.zai.dto.SeriesInput;
import pl.edu.pw.ee.zai.dto.SeriesResponse;
import pl.edu.pw.ee.zai.service.SeriesService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeriesResponse>> getAllSeries() {
        return ResponseEntity.ok(seriesService.getAllSeries());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeriesResponse> createSeries(@Valid @RequestBody SeriesInput input) {
        SeriesResponse created = seriesService.createSeries(input);
        URI location = URI.create("/api/series/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping(value = "/{seriesId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeriesResponse> getSeriesById(@PathVariable Long seriesId) {
        return ResponseEntity.ok(seriesService.getSeriesById(seriesId));
    }

    @PutMapping(value = "/{seriesId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeriesResponse> updateSeries(@PathVariable Long seriesId,
                                                       @Valid @RequestBody SeriesInput input) {
        return ResponseEntity.ok(seriesService.updateSeries(seriesId, input));
    }

    @DeleteMapping("/{seriesId}")
    public ResponseEntity<Void> deleteSeries(@PathVariable Long seriesId) {
        seriesService.deleteSeries(seriesId);
        return ResponseEntity.noContent().build();
    }
}
