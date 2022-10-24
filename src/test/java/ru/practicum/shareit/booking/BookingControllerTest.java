package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    private final Long userId = 1L;
    private final BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(1L),
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(2L),
            1L
    );
    private final BookingInfoDto bookingInfoDto = new BookingInfoDto(
            bookingDto.getId(),
            bookingDto.getStart(),
            bookingDto.getEnd(),
            BookingStatus.WAITING,
            new BookingInfoDto.UserForBookingInfoDto(1L),
            new BookingInfoDto.ItemForBookingInfoDto(1L, "itemOne")
    );

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), any())).thenReturn(bookingInfoDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingInfoDto.getId()))
                .andExpect(jsonPath("$.start").value(bookingInfoDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingInfoDto.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingInfoDto.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(bookingInfoDto.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(bookingInfoDto.getItem().getId()));

        verify(bookingService, times(1)).createBooking(any(BookingDto.class), any());
    }

    @Test
    void findBookingById() throws Exception {
        when(bookingService.findBookingById(anyLong(), any())).thenReturn(bookingInfoDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingInfoDto.getId()))
                .andExpect(jsonPath("$.start").value(bookingInfoDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingInfoDto.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingInfoDto.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(bookingInfoDto.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(bookingInfoDto.getItem().getId()));

        verify(bookingService, times(1)).findBookingById(anyLong(), any());
    }

    @Test
    void findUserBookings() throws Exception {
        List<BookingInfoDto> bookingInfoDtoRegister = List.of(bookingInfoDto);

        when(bookingService.findUserBookings(userId, "ALL", 0, 10, false))
                .thenReturn(bookingInfoDtoRegister);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingInfoDtoRegister)));

        verify(bookingService, times(1))
                .findUserBookings(userId, "ALL", 0, 10, false);
    }

    @Test
    void findBookingsForOwner() throws Exception {
        List<BookingInfoDto> bookingInfoDtoRegister = List.of(bookingInfoDto);

        when(bookingService.findUserBookings(userId, "ALL", 0, 10, true))
                .thenReturn(bookingInfoDtoRegister);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingInfoDtoRegister)));

        verify(bookingService, times(1))
                .findUserBookings(userId, "ALL", 0, 10, true);
    }

    @Test
    void changeBookingStatus() throws Exception {
        boolean approved = true;
        Long bookingId = 1L;
        BookingStatus newStatus = BookingStatus.APPROVED;

        bookingInfoDto.setStatus(newStatus);

        when(bookingService.changeBookingStatus(approved, bookingId, userId))
                .thenReturn(bookingInfoDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingInfoDto.getId()))
                .andExpect(jsonPath("$.status").value(newStatus.toString()));

        verify(bookingService, times(1)).changeBookingStatus(approved, 1L, userId);
    }
}
