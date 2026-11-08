package ua.fictionallibrary.digital_lib.audit;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String username;
    @Column
    private String action;
    @Column(nullable = false)
    private String method;
    @Column(columnDefinition = "text")
    private String args;
    @Column(columnDefinition = "text")
    private String result;
    @Column(nullable = false)
    private boolean failed;
    @Column(nullable = false)
    private Instant occurredAt;
    @Column(nullable = false)
    private long durationMs;

    protected AuditLogEntity() {}
    public AuditLogEntity(String username, String action, String method, String args, String result,
                          boolean failed, Instant occurredAt, long durationMs) {
        this.username = username;
        this.action = action;
        this.method = method;
        this.args = args;
        this.result = result;
        this.failed = failed;
        this.occurredAt = occurredAt;
        this.durationMs = durationMs;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getMethod() {
        return method;
    }

    public String getArgs() {
        return args;
    }

    public String getResult() {
        return result;
    }

    public boolean isFailed() {
        return failed;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}