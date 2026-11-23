package ua.fictionallibrary.digital_lib.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
    @Modifying
    @Query("DELETE FROM AuditLogEntity a WHERE a.occurredAt < :before")
    int deleteOlderThan(Instant before);
}