package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

import java.util.List;

public interface BookingService {
    BookingInfoDto createBooking(BookingDto bookingDto, Long bookerId);

    BookingInfoDto changeBookingStatus(Boolean approved, Long bookingId, Long userId);

    BookingInfoDto findBookingById(Long bookingId, Long userId);

    List<BookingInfoDto> findUserBookings(Long userId, String stateParam, boolean isOwner);
}
