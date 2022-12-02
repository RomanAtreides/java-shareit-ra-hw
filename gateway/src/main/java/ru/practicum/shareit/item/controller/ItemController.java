package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    public static final String HEADER_NAME_CONTAINS_OWNER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    // Добавление новой вещи
    @PostMapping
    public ResponseEntity<Object> createItem(
            @Validated({Create.class}) @RequestBody ItemDto itemDto,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        ResponseEntity<Object> newItemDto = itemClient.createItem(itemDto, userId);
        log.info("Создание предмета {}", newItemDto);
        return newItemDto;
    }

    // Добавление нового комментария
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @Validated({Create.class}) @RequestBody CommentShortDto commentShortDto,
            @PathVariable Long itemId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        ResponseEntity<Object> newCommentDto = itemClient.createComment(commentShortDto, itemId, userId);
        log.info("Создание комментария {}", newCommentDto);
        return newCommentDto;
    }

    // Просмотр информации о конкретной вещи по её идентификатору
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(
            @PathVariable Long itemId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        log.info("Получение данных вещи с id={}", itemId);
        return itemClient.findItemById(itemId, userId);
    }

    // Просмотр владельцем списка всех его вещей
    @GetMapping
    public ResponseEntity<Object> findAllUserItems(
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Просмотр владельцем списка всех его вещей");
        return itemClient.findAllUserItems(userId, from, size);
    }

    // Поиск вещи по имени или описанию
    @GetMapping("/search")
    public ResponseEntity<Object> findItemsByNameOrDescription(
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId,
            @RequestParam String text,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поиск вещи по тексту: {}", text);
        return itemClient.findItemsByNameOrDescription(userId, text, from, size);
    }

    // Редактирование вещи
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestBody ItemDto itemDto,
            @PathVariable Long itemId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        itemDto.setId(itemId);
        ResponseEntity<Object> newItemDto = itemClient.updateItem(itemDto, userId);
        log.info("Обновление предмета {}", newItemDto);
        return newItemDto;
    }
}
