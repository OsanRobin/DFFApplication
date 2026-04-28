package fenego.app.dto;

import java.time.LocalDateTime;

public class BulkActionJobDTO
{
    private Long id;
    private String type;
    private String action;
    private String attributeName;
    private String attributeValue;
    private String triggeredBy;
    private LocalDateTime date;
    private String status;
    private int processedCount;
    private String message;
    private String errors;

    public BulkActionJobDTO(
            Long id,
            String type,
            String action,
            String attributeName,
            String attributeValue,
            String triggeredBy,
            LocalDateTime date,
            String status,
            int processedCount,
            String message,
            String errors
    ) {
        this.id = id;
        this.type = type;
        this.action = action;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.triggeredBy = triggeredBy;
        this.date = date;
        this.status = status;
        this.processedCount = processedCount;
        this.message = message;
        this.errors = errors;
    }

    public Long getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public String getAction()
    {
        return action;
    }

    public String getAttributeName()
    {
        return attributeName;
    }

    public String getAttributeValue()
    {
        return attributeValue;
    }

    public String getTriggeredBy()
    {
        return triggeredBy;
    }

    public LocalDateTime getDate()
    {
        return date;
    }

    public String getStatus()
    {
        return status;
    }

    public int getProcessedCount()
    {
        return processedCount;
    }

    public String getMessage()
    {
        return message;
    }

    public String getErrors()
    {
        return errors;
    }
}