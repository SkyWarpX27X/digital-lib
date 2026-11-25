package ua.fictionallibrary.digital_lib.audit;

public interface AuditLogService {
    void save(AuditLogEntity entry);
    int purgeExpired();
}
