package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User findById(Long userId);

    List<User> findAll();

    User partialUpdate(User user);

    User delete(Long userId);
}
