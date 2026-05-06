package fenego.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import fenego.app.dto.SaveCustomerSearchRequest;
import fenego.app.dto.SavedCustomerSearchListResponse;
import fenego.app.service.SavedCustomerSearchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SavedCustomerSearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavedCustomerSearchService savedCustomerSearchService;

    @Test
    void getSavedSearches_returnsOk() throws Exception {
        Mockito.when(savedCustomerSearchService.getSavedSearches("inSPIRED"))
                .thenReturn(new SavedCustomerSearchListResponse());

        mockMvc.perform(get("/api/customer-searches")
                        .param("domain", "inSPIRED"))
                .andExpect(status().isOk());

        Mockito.verify(savedCustomerSearchService).getSavedSearches("inSPIRED");
    }

    @Test
    void getSavedSearches_withoutDomain_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/customer-searches"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveSearch_returnsOkAndCallsService() throws Exception {
        SaveCustomerSearchRequest request = new SaveCustomerSearchRequest();

        mockMvc.perform(post("/api/customer-searches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(savedCustomerSearchService).saveSearch(any(SaveCustomerSearchRequest.class));
    }

    @Test
    void deleteSearch_returnsOkAndCallsService() throws Exception {
        mockMvc.perform(delete("/api/customer-searches/1"))
                .andExpect(status().isOk());

        Mockito.verify(savedCustomerSearchService).deleteSearch(1L);
    }

    @Test
    void updateSearchName_returnsOkAndCallsService() throws Exception {
        mockMvc.perform(put("/api/customer-searches/1/name")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Nieuwe naam"))
                .andExpect(status().isOk());

        Mockito.verify(savedCustomerSearchService).updateSearchName(1L, "Nieuwe naam");
    }
}