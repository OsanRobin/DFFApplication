package fenego.app.service;

import fenego.app.dto.BulkActionRequest;
import fenego.app.dto.BulkActionResponse;
import fenego.app.dto.CustomerSegmentDTO;
import fenego.app.jpa.AttributeOption;
import fenego.app.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BulkActionService
{
    private final AuditLogService auditLogService;
    private final CustomerRepository customerRepository;

    public BulkActionService(
            AuditLogService auditLogService,
            CustomerRepository customerRepository)
    {
        this.auditLogService = auditLogService;
        this.customerRepository = customerRepository;
    }

    public List<AttributeOption> getAvailableAttributes()
    {
        return List.of(
                new AttributeOption("budgetPriceType", "Budget Price Type"),
                new AttributeOption("companyName", "Company Name"),
                new AttributeOption("customerType", "Customer Type"),
                new AttributeOption("type", "Type")
        );
    }

    public List<CustomerSegmentDTO> getAvailableSegments()
    {
        return customerRepository.findAvailableSegments();
    }

    public BulkActionResponse executeBulkAction(BulkActionRequest request)
    {
        BulkActionResponse response = new BulkActionResponse();

        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty())
        {
            response.setSuccess(false);
            response.setMessage("No customer IDs provided.");
            return response;
        }

        if (request.getAction() == null || request.getAction().isBlank())
        {
            response.setSuccess(false);
            response.setMessage("No action provided.");
            return response;
        }

        List<String> errors = new ArrayList<>();

        for (String customerId : request.getCustomerIds())
        {
            try
            {
                executeActionForCustomer(request, customerId, response);
            }
            catch (Exception e)
            {
                errors.add("Customer " + customerId + ": " + e.getMessage());
            }
        }

        response.setProcessedCount(response.getProcessedCustomerIds().size());
        response.setErrors(errors);
        response.setSuccess(errors.isEmpty());
        response.setMessage(errors.isEmpty()
                ? "Bulk action executed successfully."
                : "Bulk action failed.");

        return response;
    }

    private void executeActionForCustomer(
            BulkActionRequest request,
            String customerId,
            BulkActionResponse response)
    {
        switch (request.getAction())
        {
            case "add-attribute":
                validateAttributeAction(request);

                customerRepository.saveCustomerAttribute(
                        customerId,
                        request.getAttributeName(),
                        request.getAttributeValue()
                );

                response.getProcessedCustomerIds().add(customerId);

                auditLogService.logChange(
                        "CUSTOMER_ATTRIBUTE",
                        customerId,
                        "BULK_CREATE",
                        request.getAttributeName(),
                        "",
                        request.getAttributeValue(),
                        "system",
                        "Bulk attribute added"
                );
                break;

            case "update-attribute":
                validateAttributeAction(request);

                customerRepository.saveCustomerAttribute(
                        customerId,
                        request.getAttributeName(),
                        request.getAttributeValue()
                );

                response.getProcessedCustomerIds().add(customerId);

                auditLogService.logChange(
                        "CUSTOMER_ATTRIBUTE",
                        customerId,
                        "BULK_UPDATE",
                        request.getAttributeName(),
                        "",
                        request.getAttributeValue(),
                        "system",
                        "Bulk attribute updated"
                );
                break;

            case "delete-attribute":
                validateAttributeName(request);

                customerRepository.deleteCustomerAttribute(
                        customerId,
                        request.getAttributeName()
                );

                response.getProcessedCustomerIds().add(customerId);

                auditLogService.logChange(
                        "CUSTOMER_ATTRIBUTE",
                        customerId,
                        "BULK_DELETE",
                        request.getAttributeName(),
                        "",
                        "",
                        "system",
                        "Bulk attribute deleted"
                );
                break;

            case "assign-segment":
                validateSegmentAction(request);

                customerRepository.assignSegmentToCustomer(
                        customerId,
                        request.getSegmentId()
                );

                response.getProcessedCustomerIds().add(customerId);

                auditLogService.logChange(
                        "CUSTOMER_SEGMENT",
                        customerId,
                        "BULK_ASSIGN",
                        "segment",
                        "",
                        request.getSegmentId(),
                        "system",
                        "Bulk segment assigned"
                );
                break;

            case "update-segment":
                validateSegmentAction(request);

                customerRepository.removeAllSegmentsFromCustomer(customerId);

                customerRepository.assignSegmentToCustomer(
                        customerId,
                        request.getSegmentId()
                );

                response.getProcessedCustomerIds().add(customerId);

                auditLogService.logChange(
                        "CUSTOMER_SEGMENT",
                        customerId,
                        "BULK_UPDATE",
                        "segment",
                        "",
                        request.getSegmentId(),
                        "system",
                        "Bulk segment updated"
                );
                break;

            case "delete-segment":
                validateSegmentAction(request);

                customerRepository.removeSegmentFromCustomer(
                        customerId,
                        request.getSegmentId()
                );

                response.getProcessedCustomerIds().add(customerId);

                auditLogService.logChange(
                        "CUSTOMER_SEGMENT",
                        customerId,
                        "BULK_DELETE",
                        "segment",
                        request.getSegmentId(),
                        "",
                        "system",
                        "Bulk segment deleted"
                );
                break;

            default:
                throw new RuntimeException("Unsupported action: " + request.getAction());
        }
    }

    private void validateAttributeAction(BulkActionRequest request)
    {
        validateAttributeName(request);

        if (request.getAttributeValue() == null || request.getAttributeValue().isBlank())
        {
            throw new RuntimeException("Attribute value is missing");
        }
    }

    private void validateAttributeName(BulkActionRequest request)
    {
        if (request.getAttributeName() == null || request.getAttributeName().isBlank())
        {
            throw new RuntimeException("Attribute name is missing");
        }
    }

    private void validateSegmentAction(BulkActionRequest request)
    {
        if (request.getSegmentId() == null || request.getSegmentId().isBlank())
        {
            throw new RuntimeException("Segment ID is missing");
        }
    }
}