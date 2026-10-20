package ua.fictionallibrary.digital_lib.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class AppUserDetails extends User {
    private final UUID id;
    private final String name;

    public AppUserDetails(UUID id, String username, @Nullable String password, String name, String surname, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.name = name + " " + surname;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
}
