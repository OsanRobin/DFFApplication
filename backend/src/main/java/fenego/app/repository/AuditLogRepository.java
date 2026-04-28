package fenego.app.repository;

import fenego.app.jpa.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>
{
    List<AuditLog> findAllByOrderByChangedAtDesc();
}