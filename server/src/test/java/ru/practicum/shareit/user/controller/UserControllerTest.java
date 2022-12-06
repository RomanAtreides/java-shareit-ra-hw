package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    private final Long userId = 1L;
    private final UserDto userDto = new UserDto(1L, "test user", "test@email.com");
    private final UserDto userDtoTwo = new UserDto(2L, "userTwo", "userTwo@email.com");

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUser() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void findUserById() throws Exception {
        when(userService.findUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).findUserById(userId);
    }

    @Test
    void findAllUsers() throws Exception {
        List<UserDto> userDtoRegister = List.of(userDto, userDtoTwo);

        when(userService.findAllUsers()).thenReturn(userDtoRegister);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDtoRegister)));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void updateUser() throws Exception {
        when(userService.updateUser(userDto)).thenReturn(userDtoTwo);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDtoTwo.getId()))
                .andExpect(jsonPath("$.name").value(userDtoTwo.getName()))
                .andExpect(jsonPath("$.email").value(userDtoTwo.getEmail()));

        verify(userService, times(1)).updateUser(userDto);
    }

    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(userId);
    }
}
