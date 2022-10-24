package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    private final Long userId = 1L;
    private final ItemDto itemDto = new ItemDto(
            1L, "itemOne", "itemOne description", true, 1L
    );
    private final ItemRequestShortDto shortDto = new ItemRequestShortDto(
            1L, "request description", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    );
    private final ItemRequestDto itemRequestDto = new ItemRequestDto(
            shortDto.getId(), shortDto.getDescription(), shortDto.getCreated(), List.of(itemDto)
    );
    private final List<ItemRequestDto> requestDtoRegister = List.of(itemRequestDto);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest() throws Exception {
        when(itemRequestService.createRequest(userId, shortDto)).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(shortDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.created").value(itemRequestDto.getCreated().format(formatter)))
                .andExpect(jsonPath("$.items[0].id").value(itemRequestDto.getItems().get(0).getId()));

        verify(itemRequestService, times(1)).createRequest(userId, shortDto);
    }

    @Test
    void findUserRequests() throws Exception {
        when(itemRequestService.findUserRequests(userId)).thenReturn(requestDtoRegister);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(requestDtoRegister)));

        verify(itemRequestService, times(1)).findUserRequests(userId);
    }

    @Test
    void findAllRequests() throws Exception {
        when(itemRequestService.findAllRequests(0, 10, userId)).thenReturn(requestDtoRegister);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(requestDtoRegister)));

        verify(itemRequestService, times(1)).findAllRequests(0, 10, userId);
    }

    @Test
    void findRequestById() throws Exception {
        Long requestId = 1L;

        when(itemRequestService.findRequestById(requestId, userId)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.created").value(itemRequestDto.getCreated().format(formatter)))
                .andExpect(jsonPath("$.items[0].id").value(itemRequestDto.getItems().get(0).getId()));

        verify(itemRequestService, times(1)).findRequestById(requestId, userId);
    }
}
