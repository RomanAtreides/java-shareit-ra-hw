package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    private final Long userId = 1L;
    private final ItemInfoDto infoDto = getInfoDto();
    private final ItemDto itemDto = new ItemDto(
            1L, "itemOne", "itemOne description", true, 1L
    );
    private final ItemDto itemDtoTwo = new ItemDto(
            2L, "itemTwo", "itemTwo description", true, 2L
    );

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(any(ItemDto.class), any())).thenReturn(itemDto);

        mockMvc.perform(postJson("/items", itemDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemDto.getRequestId()));

        verify(itemService, times(1)).createItem(any(ItemDto.class), any());
    }

    @Test
    void createComment() throws Exception {
        CommentShortDto shortDto = new CommentShortDto(
                1L, "new comment", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        CommentDto commentDto = new CommentDto(
                shortDto.getId(), shortDto.getText(), "userOne", shortDto.getCreated()
        );

        when(itemService.createComment(any(CommentShortDto.class), anyLong(), any())).thenReturn(commentDto);

        mockMvc.perform(postJson("/items/1/comment", shortDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()))
                .andExpect(jsonPath("$.created").value(commentDto.getCreated().format(formatter)));

        verify(itemService, times(1))
                .createComment(any(CommentShortDto.class), anyLong(), any());
    }

    @Test
    void findItemById() throws Exception {
        when(itemService.findItemById(anyLong(), any())).thenReturn(infoDto);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(infoDto.getId()))
                .andExpect(jsonPath("$.name").value(infoDto.getName()))
                .andExpect(jsonPath("$.description").value(infoDto.getDescription()))
                .andExpect(jsonPath("$.available").value(infoDto.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").value(infoDto.getLastBooking()))
                .andExpect(jsonPath("$.nextBooking").value(infoDto.getNextBooking()))
                .andExpect(jsonPath("$.comments[0].id").value(infoDto.getComments().get(0).getId()))
                .andExpect(jsonPath("$.request.id").value(infoDto.getRequest().getId()));

        verify(itemService, times(1)).findItemById(anyLong(), any());
    }

    @Test
    void findAllUserItems() throws Exception {
        List<ItemInfoDto> itemInfoDtoRegister = List.of(infoDto);

        when(itemService.findAllUserItems(eq(userId), anyInt(), anyInt())).thenReturn(itemInfoDtoRegister);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemInfoDtoRegister)));

        verify(itemService, times(1)).findAllUserItems(eq(userId), anyInt(), anyInt());
    }

    @Test
    void findItemsByNameOrDescription() throws Exception {
        String text = "test";
        List<ItemDto> itemDtoRegister = List.of(itemDto);

        when(itemService.findItemsByNameOrDescription(eq(text), anyInt(), anyInt())).thenReturn(itemDtoRegister);

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDtoRegister)));

        verify(itemService, times(1))
                .findItemsByNameOrDescription(eq(text), anyInt(), anyInt());
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(itemDto, userId)).thenReturn(itemDtoTwo);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDtoTwo.getId()))
                .andExpect(jsonPath("$.name").value(itemDtoTwo.getName()))
                .andExpect(jsonPath("$.description").value(itemDtoTwo.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDtoTwo.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemDtoTwo.getRequestId()));

        verify(itemService, times(1)).updateItem(any(ItemDto.class), eq(userId));
    }

    private ItemInfoDto getInfoDto() {
        ItemRequest request = new ItemRequest(
                1L,
                "request description",
                new User(1L, "userOne", "userOne@email.com"),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        return new ItemInfoDto(
                1L,
                "itemOne",
                "itemOne description",
                true,
                null,
                null,
                List.of(new CommentDto(
                        1L, "text", "author", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                )),
                request
        );
    }

    private MockHttpServletRequestBuilder postJson(String uri, Object body) {
        try {
            return post(uri)
                    .header("X-Sharer-User-Id", userId)
                    .content(mapper.writeValueAsString(body))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
