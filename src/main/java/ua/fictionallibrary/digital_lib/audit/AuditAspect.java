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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository repository;
    private final TransactionTemplate transactionTemplate;

    public AuditAspect(AuditLogRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
                    Arrays.toString(pjp.getArgs()),
                    error == null ? String.valueOf(result) : error.toString(),
                    error != null,
                    start,
                    Duration.between(start, Instant.now()).toMillis());
            transactionTemplate.executeWithoutResult(_ -> repository.save(entry));
        } catch (RuntimeException e) {
            log.error("Failed to write audit log for {}", pjp.getSignature().toShortString(), e);
        }
    }

    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return "anonymous";
        return auth.getName();
    }
}