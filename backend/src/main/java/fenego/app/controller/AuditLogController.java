package fenego.app.controller;

import fenego.app.dto.AuditLogDTO;
import fenego.app.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "http://localhost:4200")
public class AuditLogController
{
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService)
    {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogDTO> getAllLogs()
    {
        return auditLogService.getAllLogs();
    }
}