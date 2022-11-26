package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.item.controller.ItemController.HEADER_NAME_CONTAINS_OWNER_ID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    // Создание нового запроса вещи
    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID) Long userId,
            @Validated({Create.class}) @RequestBody ItemRequestShortDto itemRequestShortDto) {
        ItemRequestDto itemRequestDto = itemRequestService.createRequest(userId, itemRequestShortDto);
        log.info("Создание запроса {}", itemRequestDto);
        return itemRequestDto;
    }

    // Получение списка своих запросов вместе с данными об ответах на них
    @GetMapping
    public List<ItemRequestDto> findUserRequests(
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Получение пользователем c id={} списка своих запросов", userId);
        return itemRequestService.findUserRequests(userId);
    }

    // Получение списка запросов, созданных другими пользователями
    @GetMapping("/all")
    public List<ItemRequestDto> findAllRequests(
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Получение списка запросов других пользователей");
        return itemRequestService.findAllRequests(from, size, userId);
    }

    // Получение данных об одном конкретном запросе вместе с данными об ответах на него
    @GetMapping("/{requestId}")
    public ItemRequestDto findRequestById(
            @PathVariable Long requestId,
            @RequestHeader(value = HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Получение данных запроса с id={}", requestId);
        return itemRequestService.findRequestById(requestId, userId);
    }
}
