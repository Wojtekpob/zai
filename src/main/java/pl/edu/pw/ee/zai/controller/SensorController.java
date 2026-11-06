package pl.edu.pw.ee.zai.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pw.ee.zai.dto.SensorCreatedResponse;
import pl.edu.pw.ee.zai.dto.SensorInput;
import pl.edu.pw.ee.zai.dto.SensorResponse;
import pl.edu.pw.ee.zai.service.SensorService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SensorResponse>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SensorCreatedResponse> createSensor(@Valid @RequestBody SensorInput input) {
        SensorCreatedResponse created = sensorService.createSensor(input);
        URI location = URI.create("/api/sensors/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping(value = "/{sensorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SensorResponse> getSensorById(@PathVariable Long sensorId) {
        return ResponseEntity.ok(sensorService.getSensorById(sensorId));
    }

    @DeleteMapping("/{sensorId}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Long sensorId) {
        sensorService.deleteSensor(sensorId);
        return ResponseEntity.noContent().build();
    }
}
