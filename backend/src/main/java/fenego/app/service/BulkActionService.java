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
                switch (request.getAction())
                {
                    case "add-attribute":
                        validateAttributeAction(request);

                        customerRepository.saveCustomerAttribute(
                                customerId,
                                request.getAttributeName(),
                                request.getAttributeValue()
                        );

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

                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    case "update-attribute":
                        validateAttributeAction(request);

                        customerRepository.saveCustomerAttribute(
                                customerId,
                                request.getAttributeName(),
                                request.getAttributeValue()
                        );

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

                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    case "delete-attribute":
                        validateAttributeName(request);

                        customerRepository.deleteCustomerAttribute(
                                customerId,
                                request.getAttributeName()
                        );

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

                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    case "assign-segment":
                        validateSegmentAction(request);

                        customerRepository.assignSegmentToCustomer(
                                customerId,
                                request.getSegmentId()
                        );

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

                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    case "update-segment":
                        validateSegmentAction(request);

                        customerRepository.removeAllSegmentsFromCustomer(customerId);
                        customerRepository.assignSegmentToCustomer(
                                customerId,
                                request.getSegmentId()
                        );

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

                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    case "delete-segment":
                        validateSegmentAction(request);

                        customerRepository.removeSegmentFromCustomer(
                                customerId,
                                request.getSegmentId()
                        );

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

                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    default:
                        throw new RuntimeException("Unsupported action: " + request.getAction());
                }
            }
            catch (Exception e)
            {
                errors.add("Customer " + customerId + ": " + e.getMessage());
            }
        }

        response.setProcessedCount(response.getProcessedCustomerIds().size());
        response.setErrors(errors);
        response.setSuccess(errors.isEmpty());

        if (errors.isEmpty())
        {
            response.setMessage("Bulk action executed successfully.");
        }
        else if (!response.getProcessedCustomerIds().isEmpty())
        {
            response.setMessage("Bulk action partially completed.");
        }
        else
        {
            response.setMessage("Bulk action failed.");
        }

        return response;
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