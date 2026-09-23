package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.user.UserService;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public boolean exists(UUID userId) {
        return false;
    }
}
