package ua.fictionallibrary.digital_lib.user;

import java.util.UUID;

public interface UserService {

    boolean exists(UUID userId);
}
