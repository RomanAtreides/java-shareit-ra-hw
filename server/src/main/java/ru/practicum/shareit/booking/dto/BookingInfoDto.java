package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoDto {

    private Long id;

    private LocalDateTime start;

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
