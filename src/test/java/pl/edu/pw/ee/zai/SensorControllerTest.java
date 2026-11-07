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

import static org.hamcrest.Matchers.hasLength;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private long seriesId;

    @BeforeEach
    void setUp() throws Exception {
        String loginBody = "{\"username\":\"admin\",\"password\":\"Admin123!\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        this.adminToken = jsonNode.get("accessToken").asText();

        String seriesBody = "{\"name\":\"Seria pod czujnik\",\"minValue\":-10.0,\"maxValue\":50.0,\"color\":\"#336699\"}";
        MvcResult seriesResult = mockMvc.perform(post("/api/series")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seriesBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode seriesNode = objectMapper.readTree(seriesResult.getResponse().getContentAsString());
        this.seriesId = seriesNode.get("id").asLong();
    }

    @Test
    void registerSensorReturns201AndSingleUseApiKey() throws Exception {
        String sensorBody = "{\"name\":\"Termometr #1\",\"seriesId\":" + seriesId + "}";
        mockMvc.perform(post("/api/sensors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sensorBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Termometr #1"))
                .andExpect(jsonPath("$.apiKey", hasLength(64)));
    }

    @Test
    void getSensorsDoesNotExposeApiKey() throws Exception {
        String sensorBody = "{\"name\":\"Sensor ukryty klucz\",\"seriesId\":" + seriesId + "}";
        MvcResult createResult = mockMvc.perform(post("/api/sensors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sensorBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long sensorId = createdNode.get("id").asLong();

        MvcResult getResult = mockMvc.perform(get("/api/sensors/" + sensorId)
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode getNode = objectMapper.readTree(getResult.getResponse().getContentAsString());
        assertTrue(getNode.has("id"));
        assertTrue(getNode.has("name"));
        assertTrue(getNode.has("seriesId"));
        assertFalse(getNode.hasNonNull("apiKey"));
    }

    @Test
    void unregisterSensorReturns204() throws Exception {
        String sensorBody = "{\"name\":\"Do usunięcia\",\"seriesId\":" + seriesId + "}";
        MvcResult createResult = mockMvc.perform(post("/api/sensors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sensorBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long sensorId = createdNode.get("id").asLong();

        mockMvc.perform(delete("/api/sensors/" + sensorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sensors/" + sensorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerSensorForNonExistingSeriesReturns422() throws Exception {
        String sensorBody = "{\"name\":\"Sensor widmo\",\"seriesId\":99999999}";
        mockMvc.perform(post("/api/sensors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sensorBody))
                .andExpect(status().isUnprocessableEntity());
    }
}
