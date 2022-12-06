package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utility.marker.Create;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserClient userClient;

    // Добавление нового пользователя
    @PostMapping
    public ResponseEntity<Object> createUser(@Validated({Create.class}) @RequestBody UserDto userDto) {
        ResponseEntity<Object> newUserDto = userClient.createUser(userDto);
        log.info("Создание пользователя {}", newUserDto);
        return newUserDto;
    }

    // Получение пользователя по идентификатору
    @GetMapping("/{userId}")
    public ResponseEntity<Object> findUserById(@PathVariable Long userId) {
        log.info("Получение данных пользователя с id={}", userId);
        return userClient.findUserById(userId);
    }

    // Получение списка всех пользователей
    @GetMapping
    public ResponseEntity<Object> findAllUsers() {
        log.info("Получение списка всех пользователей");
        return userClient.findAllUsers();
    }

    // Изменение пользователя
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto userDto, @PathVariable Long userId) {
        userDto.setId(userId);
        ResponseEntity<Object> newUserDto = userClient.updateUser(userDto);
        log.info("Обновление пользователя {}", newUserDto);
        return newUserDto;
    }

    // Удаление пользователя
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        userClient.deleteUser(userId);
    }
}
