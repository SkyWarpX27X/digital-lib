package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.model.UserEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, UserEntity> users;

    public InMemoryUserRepository() {
        users = new ConcurrentHashMap<>();
    }

    @Override
    public UserEntity saveUser(UserEntity entity) {
        return users.put(entity.id(), entity);
    }

    @Override
    public Optional<UserEntity> getUser(UUID userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(UUID userId) {
        users.remove(userId);
    }

    @Override
    public boolean exists(UUID userId) {
        return users.containsKey(userId);
    }
}
