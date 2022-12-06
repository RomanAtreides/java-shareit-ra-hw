package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.item.controller.ItemController.HEADER_NAME_CONTAINS_OWNER_ID;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> bookItem(
            @RequestHeader(HEADER_NAME_CONTAINS_OWNER_ID) long userId,
            @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Создание бронирования {} пользователем с id={}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(
            @RequestHeader(HEADER_NAME_CONTAINS_OWNER_ID) long userId,
            @PathVariable Long bookingId) {
        log.info("Получение данных о бронировании с id={} пользователем с id={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(
            @RequestHeader(HEADER_NAME_CONTAINS_OWNER_ID) long userId,
            @RequestParam(defaultValue = "all") String state,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        BookingState status = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        log.info("Получение данных о бронированиях пользователя с id={} со " +
                "статусом {}, from={}, size={}", userId, state, from, size);
        return bookingClient.getBookings(userId, status, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findBookingsForOwner(
            @RequestHeader(HEADER_NAME_CONTAINS_OWNER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Просмотр пользователем с id={} списка своих бронирований", userId);
        return bookingClient.findUserBookings(userId, state, from, size, true);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader(HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Изменение статуса бронирования с идентификатором {}", bookingId);
        return bookingClient.changeBookingStatus(approved, bookingId, userId);
    }
}
