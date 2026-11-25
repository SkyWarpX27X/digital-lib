package ua.fictionallibrary.digital_lib.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(auditAction)")
    public Object audit(ProceedingJoinPoint pjp, AuditAction auditAction) throws Throwable {
        Instant start = Instant.now();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            save(pjp, auditAction, start, result, error);
        }
    }

    private void save(ProceedingJoinPoint pjp, AuditAction auditAction, Instant start, Object result, Throwable error) {
        try {
            AuditLogEntity entry = new AuditLogEntity(
                    currentUser(),
                    auditAction.value(),
                    pjp.getSignature().toShortString(),
                    truncate(Arrays.toString(pjp.getArgs())),
                    truncate(error == null ? String.valueOf(result) : error.toString()),
                    error != null,
                    start,
                    Duration.between(start, Instant.now()).toMillis());
            auditLogService.save(entry);
        } catch (RuntimeException e) {
            log.error("Failed to write audit log for {}", pjp.getSignature().toShortString(), e);
        }
    }

    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return "system";
        if (!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return "anonymous";
        return auth.getName();
    }

    private static String truncate(String s) {
        return s != null && s.length() > 2000 ? s.substring(0, 2000) + "…" : s;
    }
}