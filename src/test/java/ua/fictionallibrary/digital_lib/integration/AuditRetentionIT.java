package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.fictionallibrary.digital_lib.audit.AuditLogService;
import ua.fictionallibrary.digital_lib.support.IntegrationTest;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class AuditRetentionIT {

    @Autowired
    private AuditLogService service;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void purgeExpired_deletesOnlyEntriesOlderThanRetention() {
        insert("old", Instant.now().minus(Duration.ofDays(100)));
        insert("fresh", Instant.now().minus(Duration.ofDays(1)));

        assertThat(service.purgeExpired()).isEqualTo(1);

        assertThat(jdbc.queryForList("SELECT username FROM audit_log", String.class)).containsExactly("fresh");
    }

    private void insert(String username, Instant occurredAt) {
        jdbc.update("INSERT INTO audit_log (id, username, method, failed, occurred_at, duration_ms) VALUES (?, ?, ?, false, ?, 0)",
                UUID.randomUUID(), username, "test()", Timestamp.from(occurredAt));
    }
}
