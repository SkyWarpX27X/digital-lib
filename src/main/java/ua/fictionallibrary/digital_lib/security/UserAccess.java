package ua.fictionallibrary.digital_lib.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAccess")
public class UserAccess {
    public boolean isSelf(UUID id, Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AppUserDetails user && user.getId().equals(id);
    }
}
