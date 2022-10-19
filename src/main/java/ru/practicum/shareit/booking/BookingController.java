package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    // Добавление нового запроса на бронирование
    @PostMapping
    public BookingInfoDto createBooking(
            @RequestBody BookingDto bookingDto,
            @RequestHeader(ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long bookerId) {
        BookingInfoDto bookingInfoDto = bookingService.createBooking(bookingDto, bookerId);
        log.info("Создание бронирования {}", bookingInfoDto);
        return bookingInfoDto;
    }

    // Получение данных о конкретном бронировании (включая его статус)
    @GetMapping("/{bookingId}")
    public BookingInfoDto findBookingById(
            @PathVariable Long bookingId,
            @RequestHeader(ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        return bookingService.findBookingById(bookingId, userId);
    }

    // Получение списка всех бронирований текущего пользователя
    @GetMapping
    public List<BookingInfoDto> findUserBookings(
            @RequestHeader(ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        return bookingService.findUserBookings(userId, stateParam, false);
    }

    // Получение списка бронирований для всех вещей текущего пользователя
    @GetMapping("/owner")
    List<BookingInfoDto> findBookingsForOwner(
            @RequestHeader(ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        return bookingService.findUserBookings(userId, stateParam, true);
    }

    // Подтверждение или отклонение запроса на бронирование
    @PatchMapping("/{bookingId}")
    public BookingInfoDto changeBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader(ItemController.HEADER_NAME_CONTAINS_OWNER_ID) Long userId) {
        log.info("Изменение статуса бронирования с идентификатором {}", bookingId);
        return bookingService.changeBookingStatus(approved, bookingId, userId);
    }
}
