package fenego.app.dto;

import fenego.app.jpa.AuditLog;

import java.time.LocalDateTime;

public class AuditLogDTO
{
    private Long id;
    private String entityType;
    private String entityId;
    private String action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changedAt;
    private String message;

    public AuditLogDTO(AuditLog log)
    {
        this.id = log.getId();
        this.entityType = log.getEntityType();
        this.entityId = log.getEntityId();
        this.action = log.getAction();
        this.fieldName = log.getFieldName();
        this.oldValue = log.getOldValue();
        this.newValue = log.getNewValue();
        this.changedBy = log.getChangedBy();
        this.changedAt = log.getChangedAt();
        this.message = log.getMessage();
    }

    public Long getId()
    {
        return id;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public String getAction()
    {
        return action;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public String getChangedBy()
    {
        return changedBy;
    }

    public LocalDateTime getChangedAt()
    {
        return changedAt;
    }

    public String getMessage()
    {
        return message;
    }
}