package fenego.app;

import fenego.app.dto.AuditLogDTO;
import fenego.app.jpa.AuditLog;
import fenego.app.repository.AuditLogRepository;
import fenego.app.service.AuditLogService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest
{
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void getAllLogs_shouldReturnAuditLogDTOsOrderedByChangedAtDesc()
    {
        AuditLog log1 = new AuditLog(
                "Product",
                "1",
                "UPDATE",
                "name",
                "Old name",
                "New name",
                "admin",
                "Product name updated"
        );

        AuditLog log2 = new AuditLog(
                "Order",
                "2",
                "CREATE",
                null,
                null,
                null,
                "user",
                "Order created"
        );

        when(auditLogRepository.findAllByOrderByChangedAtDesc())
                .thenReturn(List.of(log1, log2));

        List<AuditLogDTO> result = auditLogService.getAllLogs();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isInstanceOf(AuditLogDTO.class);
        assertThat(result.get(1)).isInstanceOf(AuditLogDTO.class);

        verify(auditLogRepository, times(1)).findAllByOrderByChangedAtDesc();
        verifyNoMoreInteractions(auditLogRepository);
    }

    @Test
    void getAllLogs_shouldReturnEmptyList_whenNoLogsExist()
    {
        when(auditLogRepository.findAllByOrderByChangedAtDesc())
                .thenReturn(List.of());

        List<AuditLogDTO> result = auditLogService.getAllLogs();

        assertThat(result).isEmpty();

        verify(auditLogRepository, times(1)).findAllByOrderByChangedAtDesc();
        verifyNoMoreInteractions(auditLogRepository);
    }

    @Test
    void logChange_shouldCreateAndSaveAuditLog()
    {
        auditLogService.logChange(
                "Product",
                "123",
                "UPDATE",
                "price",
                "10.00",
                "12.50",
                "admin",
                "Product price updated"
        );

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        verifyNoMoreInteractions(auditLogRepository);

        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog).isNotNull();
        assertThat(savedLog.getEntityType()).isEqualTo("Product");
        assertThat(savedLog.getEntityId()).isEqualTo("123");
        assertThat(savedLog.getAction()).isEqualTo("UPDATE");
        assertThat(savedLog.getFieldName()).isEqualTo("price");
        assertThat(savedLog.getOldValue()).isEqualTo("10.00");
        assertThat(savedLog.getNewValue()).isEqualTo("12.50");
        assertThat(savedLog.getChangedBy()).isEqualTo("admin");
        assertThat(savedLog.getMessage()).isEqualTo("Product price updated");
    }

    @Test
    void logChange_shouldAllowNullValues()
    {
        auditLogService.logChange(
                "Order",
                "456",
                "CREATE",
                null,
                null,
                null,
                "user",
                "Order created"
        );

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        verifyNoMoreInteractions(auditLogRepository);

        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog).isNotNull();
        assertThat(savedLog.getEntityType()).isEqualTo("Order");
        assertThat(savedLog.getEntityId()).isEqualTo("456");
        assertThat(savedLog.getAction()).isEqualTo("CREATE");
        assertThat(savedLog.getFieldName()).isNull();
        assertThat(savedLog.getOldValue()).isNull();
        assertThat(savedLog.getNewValue()).isNull();
        assertThat(savedLog.getChangedBy()).isEqualTo("user");
        assertThat(savedLog.getMessage()).isEqualTo("Order created");
    }
}