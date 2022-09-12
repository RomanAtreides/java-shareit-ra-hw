package ru.practicum.shareit.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.EntityAlreadyExistsException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Component
public class Validator {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Autowired
    public Validator(UserStorage userStorage, ItemStorage itemStorage) {
        this.userStorage = userStorage;
        this.itemStorage = itemStorage;
    }

    public void validateUser(User user, List<User> users) {
        if (user == null) {
            throw new EntityNotFoundException("Пользователь не найден!");
        }

        String userEmail = user.getEmail();

        if (userEmail != null && (userEmail.isBlank() || !userEmail.contains("@"))) {
            throw new ValidationException("Неверный формат электронной почты!");
        }

        if (users != null) {
            users.remove(user);

            for (User other : users) {
                if (other.getEmail().equals(userEmail)) {
                    throw new EntityAlreadyExistsException("Пользователь с такой почтой уже зарегистрирован!");
                }
            }
        }
    }

    public void checkUserId(Long userId) {
        String warning = "Пользователь с таким id не найден!";

        if (userId == null) {
            throw new ValidationException(warning);
        }

        if (userStorage.findById(userId) == null) {
            throw new EntityNotFoundException(warning);
        }
    }

    public void checkEmail(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Не указана электронная почта!");
        }
    }

    public void validateItem(Item item) {
        if (item == null) {
            throw new EntityNotFoundException("Предмет не найден!");
        }

        if (item.getAvailable() == null) {
            throw new ValidationException("Предмет недоступен!");
        }

        if (item.getName().isBlank()) {
            throw new ValidationException("Невозможно добавить предмет без имени!");
        }

        String itemDescription = item.getDescription();

        if (itemDescription == null || itemDescription.isBlank()) {
            throw new ValidationException("Невозможно добавить предмет без описания!");
        }
    }

    public void checkItemId(Long itemId) {
        if (itemId == null || itemStorage.findById(itemId) == null) {
            throw new EntityNotFoundException("Предмет с таким id не найден!");
        }
    }

    public void checkOwner(Long owner, Long userId) {
        if (!owner.equals(userId)) {
            throw new EntityNotFoundException("У указанного пользователя такой предмет не найден!");
        }
    }
}
