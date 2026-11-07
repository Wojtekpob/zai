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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

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
    }

    @Test
    void getSeriesIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/series").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createSeriesWithoutTokenReturns401() throws Exception {
        String body = "{\"name\":\"Ciśnienie\",\"minValue\":900.0,\"maxValue\":1100.0,\"color\":\"#00ff00\"}";
        mockMvc.perform(post("/api/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSeriesWithValidDataReturns201AndLocation() throws Exception {
        String body = "{\"name\":\"Temperatura\",\"minValue\":-30.0,\"maxValue\":50.0,\"color\":\"#ff0000\",\"unit\":\"°C\",\"icon\":\"thermometer\"}";
        MvcResult result = mockMvc.perform(post("/api/series")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Temperatura"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        mockMvc.perform(get(location).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Temperatura"));
    }

    @Test
    void createSeriesWithMinGreaterThanMaxReturns422() throws Exception {
        String body = "{\"name\":\"Błędny zakres\",\"minValue\":100.0,\"maxValue\":50.0,\"color\":\"#ff0000\"}";
        mockMvc.perform(post("/api/series")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateSeriesReturns200() throws Exception {
        String body = "{\"name\":\"Do modyfikacji\",\"minValue\":0.0,\"maxValue\":100.0,\"color\":\"#123456\"}";
        MvcResult createResult = mockMvc.perform(post("/api/series")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long seriesId = createdNode.get("id").asLong();

        String updateBody = "{\"name\":\"Zaktualizowana\",\"minValue\":5.0,\"maxValue\":95.0,\"color\":\"#654321\"}";
        mockMvc.perform(put("/api/series/" + seriesId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zaktualizowana"))
                .andExpect(jsonPath("$.minValue").value(5.0));
    }

    @Test
    void deleteSeriesReturns204() throws Exception {
        String body = "{\"name\":\"Do usunięcia\",\"minValue\":0.0,\"maxValue\":10.0,\"color\":\"#000000\"}";
        MvcResult createResult = mockMvc.perform(post("/api/series")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long seriesId = createdNode.get("id").asLong();

        mockMvc.perform(delete("/api/series/" + seriesId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/series/" + seriesId))
                .andExpect(status().isNotFound());
    }
}
