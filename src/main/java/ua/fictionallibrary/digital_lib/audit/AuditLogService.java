package ua.fictionallibrary.digital_lib.audit;

public interface AuditLogService {
    void record(AuditLogEntity entry);
    int purgeExpired();
}
