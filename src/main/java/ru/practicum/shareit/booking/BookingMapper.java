package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Component
public class BookingMapper {
    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                item,
                booker,
                BookingStatus.WAITING
        );
    }

    public static BookingInfoDto toBookingInfoDto(Booking booking) {
        return new BookingInfoDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new BookingInfoDto.UserForBookingInfoDto(booking.getBooker().getId()),
                new BookingInfoDto.ItemForBookingInfoDto(booking.getItem().getId(), booking.getItem().getName())
        );
    }
}
