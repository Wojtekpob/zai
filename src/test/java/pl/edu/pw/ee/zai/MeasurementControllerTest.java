package pl.edu.pw.ee.zai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private long seriesId;
    private String sensorApiKey;

    @BeforeEach
    void setUp() throws Exception {
        String loginBody = "{\"username\":\"admin\",\"password\":\"Admin123!\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        this.adminToken = loginNode.get("accessToken").asText();

        String seriesBody = "{\"name\":\"Wilgotność\",\"minValue\":0.0,\"maxValue\":100.0,\"color\":\"#00aaff\"}";
        MvcResult seriesResult = mockMvc.perform(post("/api/series")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode seriesNode = objectMapper.readTree(seriesResult.getResponse().getContentAsString());
        this.seriesId = seriesNode.get("id").asLong();

        String sensorBody = "{\"name\":\"Higrometr #1\",\"seriesId\":" + seriesId + "}";
        MvcResult sensorResult = mockMvc.perform(post("/api/sensors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sensorBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode sensorNode = objectMapper.readTree(sensorResult.getResponse().getContentAsString());
        this.sensorApiKey = sensorNode.get("apiKey").asText();
    }

    @Test
    void postMeasurementWithValidKeyAndRangeReturns201() throws Exception {
        String measurementBody = "{\"value\":45.5}";
        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(measurementBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.value").value(45.5))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void postMeasurementWithBoundaryValuesIsAccepted() throws Exception {
        String minMeasurement = "{\"value\":0.0}";
        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(minMeasurement))
                .andExpect(status().isCreated());

        String maxMeasurement = "{\"value\":100.0}";
        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maxMeasurement))
                .andExpect(status().isCreated());
    }

    @Test
    void postMeasurementOutOfRangeReturns422() throws Exception {
        String measurementBody = "{\"value\":105.0}";
        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(measurementBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void postMeasurementWithFutureTimestampReturns422() throws Exception {
        Instant future = Instant.now().plus(1, ChronoUnit.HOURS);
        String measurementBody = "{\"value\":50.0,\"timestamp\":\"" + future.toString() + "\"}";

        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(measurementBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void postMeasurementWithoutApiKeyReturns401() throws Exception {
        String measurementBody = "{\"value\":50.0}";
        mockMvc.perform(post("/api/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(measurementBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postMeasurementWithAdminTokenInsteadOfApiKeyReturns401() throws Exception {
        String measurementBody = "{\"value\":50.0}";
        mockMvc.perform(post("/api/measurements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(measurementBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void queryMeasurementsSortingAndFiltering() throws Exception {
        Instant t1 = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant t2 = Instant.now().minus(20, ChronoUnit.MINUTES);
        Instant t3 = Instant.now().minus(10, ChronoUnit.MINUTES);

        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":10.0,\"timestamp\":\"" + t1.toString() + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":20.0,\"timestamp\":\"" + t2.toString() + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":30.0,\"timestamp\":\"" + t3.toString() + "\"}"))
                .andExpect(status().isCreated());

        MvcResult ascResult = mockMvc.perform(get("/api/measurements?series=" + seriesId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode ascNode = objectMapper.readTree(ascResult.getResponse().getContentAsString());
        assertTrue(ascNode.isArray());
        assertTrue(ascNode.size() >= 3);
        double firstVal = ascNode.get(0).get("value").asDouble();
        double lastVal = ascNode.get(ascNode.size() - 1).get("value").asDouble();
        assertTrue(firstVal <= lastVal);

        MvcResult descResult = mockMvc.perform(get("/api/measurements?series=" + seriesId + "&sort=-timestamp")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode descNode = objectMapper.readTree(descResult.getResponse().getContentAsString());
        assertEquals(30.0, descNode.get(0).get("value").asDouble());

        MvcResult filteredResult = mockMvc.perform(get("/api/measurements?series=" + seriesId +
                        "&from=" + t1.minus(1, ChronoUnit.MINUTES).toString() +
                        "&to=" + t2.plus(1, ChronoUnit.MINUTES).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode filteredNode = objectMapper.readTree(filteredResult.getResponse().getContentAsString());
        assertEquals(2, filteredNode.size());
    }

    @Test
    void updateSeriesRangeExcludingMeasurementsReturns409Conflict() throws Exception {
        mockMvc.perform(post("/api/measurements")
                        .header("X-API-Key", sensorApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":85.0}"))
                .andExpect(status().isCreated());

        String conflictingUpdate = "{\"name\":\"Wilgotność\",\"minValue\":0.0,\"maxValue\":80.0,\"color\":\"#00aaff\"}";
        mockMvc.perform(put("/api/series/" + seriesId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflictingUpdate))
                .andExpect(status().isConflict());
    }
}
