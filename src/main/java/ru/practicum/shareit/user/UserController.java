package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto create(@RequestBody User user) {
        return UserMapper.toUserDto(userService.create(user));
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        return UserMapper.toUserDto(userService.findById(userId));
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{userId}")
    public UserDto partialUpdate(@RequestBody User user, @PathVariable Long userId) {
        return UserMapper.toUserDto(userService.partialUpdate(user, userId));
    }

    @DeleteMapping("/{userId}")
    public UserDto delete(@PathVariable Long userId) {
        return UserMapper.toUserDto(userService.delete(userId));
    }
}
