package fenego.app.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityType;
    private String entityId;
    private String action;
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    private String changedBy;
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String message;

    public AuditLog()
    {
    }

    public AuditLog(
            String entityType,
            String entityId,
            String action,
            String fieldName,
            String oldValue,
            String newValue,
            String changedBy,
            String message)
    {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.message = message;
        this.changedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist()
    {
        if (changedAt == null)
        {
            changedAt = LocalDateTime.now();
        }
    }

    public Long getId()
    {
        return id;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public String getChangedBy()
    {
        return changedBy;
    }

    public void setChangedBy(String changedBy)
    {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt()
    {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt)
    {
        this.changedAt = changedAt;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}