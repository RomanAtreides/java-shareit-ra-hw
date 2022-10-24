package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    public static final String HEADER_NAME_CONTAINS_OWNER_ID = "X-Sharer-User-Id";

    // Добавление новой вещи
    @PostMapping
    public ItemDto createItem(
            @Validated({Create.class}) @RequestBody ItemDto itemDto,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        ItemDto newItemDto = itemService.createItem(itemDto, userId);
        log.info("Создание предмета {}", newItemDto);
        return newItemDto;
    }

    // Добавление нового комментария
    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @Validated({Create.class}) @RequestBody CommentShortDto commentShortDto,
            @PathVariable Long itemId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        CommentDto newCommentDto = itemService.createComment(commentShortDto, itemId, userId);
        log.info("Создание комментария {}", newCommentDto);
        return newCommentDto;
    }

    // Просмотр информации о конкретной вещи по её идентификатору
    @GetMapping("/{itemId}")
    public ItemInfoDto findItemById(
            @PathVariable Long itemId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        log.info("Получение данных вещи с id={}", itemId);
        return itemService.findItemById(itemId, userId);
    }

    // Просмотр владельцем списка всех его вещей
    @GetMapping
    public List<ItemInfoDto> findAllUserItems(
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Просмотр владельцем списка всех его вещей");
        return itemService.findAllUserItems(userId, from, size);
    }

    // Поиск вещи по имени или описанию
    @GetMapping("/search")
    public List<ItemDto> findItemsByNameOrDescription(
            @RequestParam String text,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Поиск вещи по тексту: {}", text);
        return itemService.findItemsByNameOrDescription(text, from, size);
    }

    // Редактирование вещи
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestBody ItemDto itemDto,
            @PathVariable Long itemId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID, required = false) Long userId) {
        itemDto.setId(itemId);
        ItemDto newItemDto = itemService.updateItem(itemDto, userId);
        log.info("Обновление предмета {}", newItemDto);
        return newItemDto;
    }
}
