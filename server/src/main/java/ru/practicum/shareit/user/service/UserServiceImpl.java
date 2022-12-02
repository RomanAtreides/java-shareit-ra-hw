package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Добавление нового пользователя
    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        final User user = UserMapper.toUser(userDto);
        final User entity = userRepository.save(user);
        return UserMapper.toUserDto(entity);
    }

    // Получение пользователя по идентификатору
    @Override
    public UserDto findUserById(Long userId) {
        return UserMapper.toUserDto(getUserIfExists(userId));
    }

    // Получение списка всех пользователей
    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    // Изменение пользователя
    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        Long userId = userDto.getId();
        final User userToUpdate = getUserIfExists(userId);
        final User user = UserMapper.toUser(userDto);
        String name = user.getName();
        String email = user.getEmail();

        if (name != null) {
            userToUpdate.setName(name);
        }

        if (email != null && !email.isBlank()) {
            userToUpdate.setEmail(email);
        }
        return UserMapper.toUserDto(userRepository.save(userToUpdate));
    }

    // Удаление пользователя
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = UserMapper.toUser(findUserById(userId));
        userRepository.delete(user);
    }

    private User getUserIfExists(Long userId) {
        String exceptionMessage = "Пользователь с id " + userId + " не найден!";

        if (userId == null) {
            throw new ValidationException(exceptionMessage);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(exceptionMessage));
    }
}
