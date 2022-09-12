package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */

@Builder
@Data
public class Booking {
    private final Long id;
    private final LocalDate start;
    private final LocalDate end;
    private final Item item;
    private final User booker;
    private final String status;
    /*
     * Может принимать одно из следующих значений:
     *      WAITING - новое бронирование
     *      APPROVED - бронирование подтверждено владельцем
     *      CANCELED - бронирование отменено создателем
     */
}
