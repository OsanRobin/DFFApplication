package fenego.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import fenego.app.dto.BulkActionRequest;
import fenego.app.dto.BulkActionResponse;
import fenego.app.service.BulkActionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BulkActionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BulkActionService bulkActionService;

    @Test
    void getAvailableAttributes_returnsOk() throws Exception {
        Mockito.when(bulkActionService.getAvailableAttributes()).thenReturn(List.of());

        mockMvc.perform(get("/api/bulk-actions/attributes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAvailableSegments_returnsOk() throws Exception {
        Mockito.when(bulkActionService.getAvailableSegments()).thenReturn(List.of());

        mockMvc.perform(get("/api/bulk-actions/segments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void executeBulkAction_returnsOk() throws Exception {
        BulkActionRequest request = new BulkActionRequest();

        Mockito.when(bulkActionService.executeBulkAction(any(BulkActionRequest.class)))
                .thenReturn(new BulkActionResponse());

        mockMvc.perform(post("/api/bulk-actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(bulkActionService).executeBulkAction(any(BulkActionRequest.class));
    }
}