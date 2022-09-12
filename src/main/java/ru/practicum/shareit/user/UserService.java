package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.utility.Validator;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Validator validator;

    @Autowired
    public UserService(UserStorage userStorage, Validator validator) {
        this.userStorage = userStorage;
        this.validator = validator;
    }

    public User create(User user) {
        validator.validateUser(user, userStorage.findAll());
        validator.checkEmail(user);
        return userStorage.create(user);
    }

    public User findById(Long userId) {
        User user = userStorage.findById(userId);

        validator.validateUser(user, userStorage.findAll());
        return user;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User partialUpdate(User user, Long userId) {
        validator.checkUserId(userId);
        validator.validateUser(user, userStorage.findAll());
        return userStorage.partialUpdate(update(user, userId));
    }

    public User delete(Long userId) {
        return userStorage.delete(userId);
    }

    private User update(User user, Long userId) {
        User oldUser = userStorage.findById(userId);
        String oldUserName = oldUser.getName();
        String oldUserEmail = oldUser.getEmail();
        String userName = user.getName();
        String userEmail = user.getEmail();

        if (userName != null && !userName.equals(oldUserName)) {
            oldUser.setName(userName);
        }

        if (userEmail != null && !userEmail.equals(oldUserEmail)) {
            oldUser.setEmail(userEmail);
        }
        return oldUser;
    }
}
