package fenego.app.service;

import fenego.app.dto.BulkActionRequest;
import fenego.app.dto.BulkActionResponse;
import fenego.app.jpa.AttributeOption;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BulkActionService
{
    public List<AttributeOption> getAvailableAttributes()
    {
        return List.of(
                new AttributeOption("budgetPriceType", "Budget Price Type"),
                new AttributeOption("companyName", "Company Name"),
                new AttributeOption("customerType", "Customer Type"),
                new AttributeOption("type", "Type")
        );
    }

  

    public BulkActionResponse executeBulkAction(BulkActionRequest request)
    {
        BulkActionResponse response = new BulkActionResponse();

        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            response.setSuccess(false);
            response.setMessage("No customer IDs provided.");
            return response;
        }

        if (request.getAction() == null || request.getAction().isBlank()) {
            response.setSuccess(false);
            response.setMessage("No action provided.");
            return response;
        }

        List<String> errors = new ArrayList<>();

        for (String customerId : request.getCustomerIds()) {
            try {
                switch (request.getAction()) {
                    case "add-attribute":
                    case "update-attribute":
                        validateAttributeAction(request);
                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    case "assign-segment":
                        validateSegmentAction(request);
                        response.getProcessedCustomerIds().add(customerId);
                        break;

                    default:
                        throw new RuntimeException("Unsupported action: " + request.getAction());
                }
            } catch (Exception e) {
                errors.add("Customer " + customerId + ": " + e.getMessage());
            }
        }

        response.setProcessedCount(response.getProcessedCustomerIds().size());
        response.setErrors(errors);
        response.setSuccess(errors.isEmpty());

        if (errors.isEmpty()) {
            response.setMessage("Bulk action executed successfully.");
        } else if (!response.getProcessedCustomerIds().isEmpty()) {
            response.setMessage("Bulk action partially completed.");
        } else {
            response.setMessage("Bulk action failed.");
        }

        return response;
    }

    private void validateAttributeAction(BulkActionRequest request)
    {
        if (request.getAttributeName() == null || request.getAttributeName().isBlank()) {
            throw new RuntimeException("Attribute name is missing");
        }

        if (request.getAttributeValue() == null || request.getAttributeValue().isBlank()) {
            throw new RuntimeException("Attribute value is missing");
        }
    }

    private void validateSegmentAction(BulkActionRequest request)
    {
        if (request.getSegmentId() == null || request.getSegmentId().isBlank()) {
            throw new RuntimeException("Segment ID is missing");
        }
    }
}