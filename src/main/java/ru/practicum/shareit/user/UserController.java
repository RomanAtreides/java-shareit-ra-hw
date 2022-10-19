package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.marker.Create;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    // Добавление нового пользователя
    @PostMapping
    public UserDto createUser(@Validated({Create.class}) @RequestBody UserDto userDto) {
        UserDto newUserDto = userService.createUser(userDto);
        log.info("Создание пользователя {}", newUserDto);
        return newUserDto;
    }

    // Получение пользователя по идентификатору
    @GetMapping("/{userId}")
    public UserDto findUserById(@PathVariable Long userId) {
        return userService.findUserById(userId);
    }

    // Получение списка всех пользователей
    @GetMapping
    public List<UserDto> findAllUsers() {
        return userService.findAllUsers();
    }

    // Изменение пользователя
    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable Long userId) {
        userDto.setId(userId);
        UserDto newUserDto = userService.updateUser(userDto);
        log.info("Обновление пользователя {}", newUserDto);
        return newUserDto;
    }

    // Удаление пользователя
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с идентификатором {}", userId);
        userService.deleteUser(userId);
    }
}
