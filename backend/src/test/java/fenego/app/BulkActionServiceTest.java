package fenego.app;

import fenego.app.dto.BulkActionRequest;
import fenego.app.dto.BulkActionResponse;
import fenego.app.dto.CustomerSegmentDTO;
import fenego.app.jpa.AttributeOption;
import fenego.app.repository.CustomerRepository;
import fenego.app.service.AuditLogService;
import fenego.app.service.BulkActionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkActionServiceTest
{
    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BulkActionService bulkActionService;

    @Test
    void getAvailableAttributes_shouldReturnDefaultAttributes()
    {
        List<AttributeOption> result = bulkActionService.getAvailableAttributes();

        assertThat(result).hasSize(4);

        assertThat(result)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                        new AttributeOption("budgetPriceType", "Budget Price Type"),
                        new AttributeOption("companyName", "Company Name"),
                        new AttributeOption("customerType", "Customer Type"),
                        new AttributeOption("type", "Type")
                );
    }

    @Test
    void getAvailableSegments_shouldReturnSegmentsFromRepository()
    {
        List<CustomerSegmentDTO> segments = List.of(
                mock(CustomerSegmentDTO.class),
                mock(CustomerSegmentDTO.class)
        );

        when(customerRepository.findAvailableSegments()).thenReturn(segments);

        List<CustomerSegmentDTO> result = bulkActionService.getAvailableSegments();

        assertThat(result).isEqualTo(segments);

        verify(customerRepository, times(1)).findAvailableSegments();
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void executeBulkAction_shouldFail_whenCustomerIdsAreNull()
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setAction("add-attribute");
        request.setCustomerIds(null);

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No customer IDs provided.");

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void executeBulkAction_shouldFail_whenCustomerIdsAreEmpty()
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setAction("add-attribute");
        request.setCustomerIds(List.of());

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No customer IDs provided.");

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void executeBulkAction_shouldFail_whenActionIsNull()
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setCustomerIds(List.of("C001"));
        request.setAction(null);

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No action provided.");

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void executeBulkAction_shouldFail_whenActionIsBlank()
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setCustomerIds(List.of("C001"));
        request.setAction("   ");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No action provided.");

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void executeBulkAction_shouldAddAttributeForAllCustomers()
    {
        BulkActionRequest request = attributeRequest("add-attribute");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Bulk action executed successfully.");
        assertThat(response.getProcessedCount()).isEqualTo(2);
        assertThat(response.getProcessedCustomerIds()).containsExactly("C001", "C002");
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void executeBulkAction_shouldUpdateAttributeForAllCustomers()
    {
        BulkActionRequest request = attributeRequest("update-attribute");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Bulk action executed successfully.");
        assertThat(response.getProcessedCount()).isEqualTo(2);
        assertThat(response.getProcessedCustomerIds()).containsExactly("C001", "C002");
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void executeBulkAction_shouldDeleteAttributeForAllCustomers()
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setAction("delete-attribute");
        request.setCustomerIds(List.of("C001", "C002"));
        request.setAttributeName("customerType");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProcessedCount()).isEqualTo(2);
    }

    @Test
    void executeBulkAction_shouldAssignSegmentForAllCustomers()
    {
        BulkActionRequest request = segmentRequest("assign-segment");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProcessedCount()).isEqualTo(2);
    }

    @Test
    void executeBulkAction_shouldUpdateSegmentForAllCustomers()
    {
        BulkActionRequest request = segmentRequest("update-segment");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProcessedCount()).isEqualTo(2);
    }

    @Test
    void executeBulkAction_shouldDeleteSegmentForAllCustomers()
    {
        BulkActionRequest request = segmentRequest("delete-segment");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProcessedCount()).isEqualTo(2);
    }

    @Test
    void executeBulkAction_shouldFail_whenUnsupportedAction()
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setAction("unknown-action");
        request.setCustomerIds(List.of("C001"));

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProcessedCount()).isZero();
    }

    @Test
    void executeBulkAction_shouldFail_whenAttributeNameIsMissing()
    {
        BulkActionRequest request = attributeRequest("add-attribute");
        request.setAttributeName(" ");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProcessedCount()).isZero();
    }

    @Test
    void executeBulkAction_shouldFail_whenAttributeValueIsMissing()
    {
        BulkActionRequest request = attributeRequest("update-attribute");
        request.setAttributeValue("");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProcessedCount()).isZero();
    }

    @Test
    void executeBulkAction_shouldFail_whenSegmentIdIsMissing()
    {
        BulkActionRequest request = segmentRequest("assign-segment");
        request.setSegmentId(" ");

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProcessedCount()).isZero();
    }

    @Test
    void executeBulkAction_shouldPartiallyComplete_whenOneCustomerFails()
    {
        BulkActionRequest request = attributeRequest("add-attribute");

        doAnswer(invocation -> {
            String customerId = invocation.getArgument(0);

            if ("C002".equals(customerId))
            {
                throw new RuntimeException("Database error");
            }

            return null;
        }).when(customerRepository)
                .saveCustomerAttribute(anyString(), anyString(), anyString());

        BulkActionResponse response = bulkActionService.executeBulkAction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Bulk action failed.");
        assertThat(response.getProcessedCount()).isEqualTo(1);
        assertThat(response.getProcessedCustomerIds()).containsExactly("C001");
        assertThat(response.getErrors()).containsExactly(
                "Customer C002: Database error"
        );

        verify(customerRepository).saveCustomerAttribute("C001", "customerType", "B2B");
        verify(customerRepository).saveCustomerAttribute("C002", "customerType", "B2B");
    }

    private BulkActionRequest attributeRequest(String action)
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setAction(action);
        request.setCustomerIds(List.of("C001", "C002"));
        request.setAttributeName("customerType");
        request.setAttributeValue("B2B");

        return request;
    }

    private BulkActionRequest segmentRequest(String action)
    {
        BulkActionRequest request = new BulkActionRequest();
        request.setAction(action);
        request.setCustomerIds(List.of("C001", "C002"));
        request.setSegmentId("S001");

        return request;
    }
}