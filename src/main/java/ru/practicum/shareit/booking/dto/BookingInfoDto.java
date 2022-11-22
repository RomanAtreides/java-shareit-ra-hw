package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoDto {

    private Long id;

    @FutureOrPresent
    private LocalDateTime start;

    @Future
    private LocalDateTime end;

    private BookingStatus status;

    private UserForBookingInfoDto booker;

    private ItemForBookingInfoDto item;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserForBookingInfoDto {
        private Long id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemForBookingInfoDto {
        private Long id;
        private String name;
    }
}
