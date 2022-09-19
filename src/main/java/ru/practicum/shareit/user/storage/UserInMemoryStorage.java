package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserInMemoryStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @Override
    public User create(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(Long userId) {
        return users.get(userId);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User partialUpdate(User user) {
        return user;
    }

    @Override
    public User delete(Long userId) {
        return users.remove(userId);
    }

    private Long generateId() {
        return id++;
    }
}
