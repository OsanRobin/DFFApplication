package fenego.app.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkActionResponse
{
    private boolean success;
    private String message;
    private List<String> processedCustomerIds = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public int getProcessedCount()
    {
        return processedCustomerIds == null ? 0 : processedCustomerIds.size();
    }

    public void setProcessedCount(int processedCount)
    {
        // bewust leeg: count wordt berekend uit processedCustomerIds
    }

    public List<String> getProcessedCustomerIds()
    {
        if (processedCustomerIds == null)
        {
            processedCustomerIds = new ArrayList<>();
        }

        return processedCustomerIds;
    }

    public void setProcessedCustomerIds(List<String> processedCustomerIds)
    {
        this.processedCustomerIds = processedCustomerIds == null
                ? new ArrayList<>()
                : processedCustomerIds;
    }

    public List<String> getErrors()
    {
        if (errors == null)
        {
            errors = new ArrayList<>();
        }

        return errors;
    }

    public void setErrors(List<String> errors)
    {
        this.errors = errors == null
                ? new ArrayList<>()
                : errors;
    }
}