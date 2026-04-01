package fenego.app.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkActionResponse
{
    private boolean success;
    private String message;
    private int processedCount;
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
        return processedCount;
    }

    public void setProcessedCount(int processedCount)
    {
        this.processedCount = processedCount;
    }

    public List<String> getProcessedCustomerIds()
    {
        return processedCustomerIds;
    }

    public void setProcessedCustomerIds(List<String> processedCustomerIds)
    {
        this.processedCustomerIds = processedCustomerIds;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public void setErrors(List<String> errors)
    {
        this.errors = errors;
    }
}