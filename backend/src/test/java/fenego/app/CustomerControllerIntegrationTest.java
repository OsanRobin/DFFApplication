package fenego.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import fenego.app.dto.CustomerAttributeRequest;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerSegmentRequest;
import fenego.app.service.CustomerService;
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
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void getCustomers_withRequiredHeadersAndParams_returnsOk() throws Exception {
        Mockito.when(customerService.getCustomers(
                anyString(),
                anyString(),
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyBoolean()
        )).thenReturn(new CustomerListResponse());

        mockMvc.perform(get("/api/customers")
                        .header("authentication-token", "token-123")
                        .param("domain", "inSPIRED")
                        .param("offset", "0")
                        .param("limit", "50")
                        .sessionAttr("user", "admin")
                        .sessionAttr("managerRestricted", false))
                .andExpect(status().isOk());

        Mockito.verify(customerService).getCustomers(
                eq("token-123"),
                eq("inSPIRED"),
                eq(0),
                eq(50),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("admin"),
                eq(false)
        );
    }

    @Test
    void getCustomers_withoutAuthenticationToken_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .param("domain", "inSPIRED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCustomerAttribute_returnsOkAndCallsService() throws Exception {
        CustomerAttributeRequest request = new CustomerAttributeRequest();

        mockMvc.perform(post("/api/customers/C1000/attributes")
                        .header("authentication-token", "token-123")
                        .param("domain", "inSPIRED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(customerService).addCustomerAttribute(
                eq("token-123"),
                eq("inSPIRED"),
                eq("C1000"),
                any(CustomerAttributeRequest.class)
        );
    }

    @Test
    void updateCustomerAttribute_returnsOkAndCallsService() throws Exception {
        CustomerAttributeRequest request = new CustomerAttributeRequest();

        mockMvc.perform(put("/api/customers/C1000/attributes")
                        .header("authentication-token", "token-123")
                        .param("domain", "inSPIRED")
                        .param("attributeName", "customerType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(customerService).updateCustomerAttribute(
                eq("token-123"),
                eq("inSPIRED"),
                eq("C1000"),
                eq("customerType"),
                any(CustomerAttributeRequest.class)
        );
    }

    @Test
    void deleteCustomerAttribute_returnsOkAndCallsService() throws Exception {
        mockMvc.perform(delete("/api/customers/C1000/attributes")
                        .header("authentication-token", "token-123")
                        .param("domain", "inSPIRED")
                        .param("attributeName", "customerType"))
                .andExpect(status().isOk());

        Mockito.verify(customerService).deleteCustomerAttribute(
                eq("token-123"),
                eq("inSPIRED"),
                eq("C1000"),
                eq("customerType")
        );
    }

    @Test
    void createSegment_returnsOkAndCallsService() throws Exception {
        CustomerSegmentRequest request = new CustomerSegmentRequest();

        mockMvc.perform(post("/api/customers/segments")
                        .header("authentication-token", "token-123")
                        .param("domain", "inSPIRED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(customerService).createSegment(
                eq("token-123"),
                eq("inSPIRED"),
                any(CustomerSegmentRequest.class)
        );
    }

    @Test
    void deleteSegment_returnsOkAndCallsService() throws Exception {
        mockMvc.perform(delete("/api/customers/segments/S1")
                        .header("authentication-token", "token-123")
                        .param("domain", "inSPIRED"))
                .andExpect(status().isOk());

        Mockito.verify(customerService).deleteSegment(
                eq("token-123"),
                eq("inSPIRED"),
                eq("S1")
        );
    }
}