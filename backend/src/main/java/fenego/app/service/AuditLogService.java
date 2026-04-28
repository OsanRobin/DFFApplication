package fenego.app.service;

import fenego.app.dto.AuditLogDTO;
import fenego.app.jpa.AuditLog;
import fenego.app.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService
{
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository)
    {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogDTO> getAllLogs()
    {
        return auditLogRepository.findAllByOrderByChangedAtDesc()
                .stream()
                .map(AuditLogDTO::new)
                .toList();
    }

    public void logChange(
            String entityType,
            String entityId,
            String action,
            String fieldName,
            String oldValue,
            String newValue,
            String changedBy,
            String message)
    {
        AuditLog log = new AuditLog(
                entityType,
                entityId,
                action,
                fieldName,
                oldValue,
                newValue,
                changedBy,
                message
        );

        auditLogRepository.save(log);
    }
}