package ua.fictionallibrary.digital_lib.audit.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.audit.AuditLogEntity;
import ua.fictionallibrary.digital_lib.audit.AuditLogRepository;
import ua.fictionallibrary.digital_lib.audit.AuditLogService;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    private final AuditLogRepository repository;
    private final Duration retention;

    public AuditLogServiceImpl(AuditLogRepository repository, @Value("${app.audit.retention}") Duration retention) {
        this.repository = repository;
        this.retention = retention;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditLogEntity entry) {
        repository.save(entry);
    }

    @Override
    @Transactional
    public int purgeExpired() {
        Instant before = Instant.now().minus(retention);
        log.info("Audit log retention started: deleting entries older than {}", before);
        int deleted = repository.deleteOlderThan(before);
        log.info("Audit log retention finished: deleted {} entries", deleted);
        return deleted;
    }
}
