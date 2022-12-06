package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    // Создание нового запроса вещи
    @PostMapping
    public ResponseEntity<Object> createRequest(
            @RequestHeader(value = ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId,
            @Validated({Create.class}) @RequestBody ItemRequestShortDto itemRequestShortDto) {
        ResponseEntity<Object> itemRequestDto = itemRequestClient.createRequest(userId, itemRequestShortDto);
        log.info("Создание запроса {}", itemRequestDto);
        return itemRequestDto;
    }

    // Получение списка своих запросов вместе с данными об ответах на них
    @GetMapping
    public ResponseEntity<Object> findUserRequests(
            @RequestHeader(value = ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Получение пользователем c id={} списка своих запросов", userId);
        return itemRequestClient.findUserRequests(userId);
    }

    // Получение списка запросов, созданных другими пользователями
    @GetMapping("/all")
    public ResponseEntity<Object> findAllRequests(
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Получение списка запросов других пользователей");
        return itemRequestClient.findAllRequests(from, size, userId);
    }

    // Получение данных об одном конкретном запросе вместе с данными об ответах на него
    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findRequestById(
            @PathVariable Long requestId,
            @RequestHeader(value = ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Получение данных запроса с id={}", requestId);
        return itemRequestClient.findRequestById(requestId, userId);
    }
}
