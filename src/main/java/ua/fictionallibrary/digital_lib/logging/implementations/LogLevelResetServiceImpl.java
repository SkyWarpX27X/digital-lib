package ua.fictionallibrary.digital_lib.logging.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.logging.LogLevelResetService;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogLevelResetServiceImpl implements LogLevelResetService {
    private static final Logger log = LoggerFactory.getLogger(LogLevelResetServiceImpl.class);
    private static final Set<LogLevel> DIAGNOSTIC = Set.of(LogLevel.DEBUG, LogLevel.TRACE);

    private final LoggingSystem loggingSystem;
    private final Duration resetAfter;
    private final Map<String, LogLevel> baseLevels = new ConcurrentHashMap<>();
    private final Map<String, Instant> firstSeen = new ConcurrentHashMap<>();

    public LogLevelResetServiceImpl(LoggingSystem loggingSystem,
                                    @Value("${app.logging.level-reset-after}") Duration resetAfter) {
        this.loggingSystem = loggingSystem;
        this.resetAfter = resetAfter;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rememberBaseLevels() {
        for (LoggerConfiguration config : loggingSystem.getLoggerConfigurations())
            if (config.getConfiguredLevel() != null)
                baseLevels.put(config.getName(), config.getConfiguredLevel());
    }

    @Override
    public void check(Instant now) {
        Set<String> raised = new HashSet<>();
        for (LoggerConfiguration config : loggingSystem.getLoggerConfigurations()) {
            String name = config.getName();
            LogLevel level = config.getConfiguredLevel();
            LogLevel base = baseLevels.get(name);
            if (level == null || !DIAGNOSTIC.contains(level) || level == base)
                continue;
            raised.add(name);
            Instant seen = firstSeen.computeIfAbsent(name, n -> now);
            if (Duration.between(seen, now).compareTo(resetAfter) > 0) {
                loggingSystem.setLogLevel(name, base);
                firstSeen.remove(name);
                log.info("Рівень логера {} повернуто з {} на {}", name, level, base == null ? "успадкований" : base);
            }
        }
        firstSeen.keySet().retainAll(raised);
        log.debug("Log level reset check: {} raised logger(s) being watched", firstSeen.size());
    }
}
