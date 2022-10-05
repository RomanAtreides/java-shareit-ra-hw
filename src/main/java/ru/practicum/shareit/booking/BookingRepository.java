package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.item.id = ?1 and b.end < ?2")
    List<Booking> findLastBooking(Long itemId, LocalDateTime now);

    @Query("select b from Booking b where b.item.id = ?1 and b.start > ?2")
    List<Booking> findNextBooking(Long itemId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start > ?2 order by b.id desc")
    List<Booking> findOwnerBookingsWithStartIsAfter(Long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start > ?2 order by b.id desc")
    List<Booking> findUserBookingsWithStartIsAfter(Long userId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findCurrentOwnerBookings(Long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findCurrentUserBookings(Long userId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.end < ?2 order by b.id desc")
    List<Booking> findOwnerBookingsWithEndIsBefore(Long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.end < ?2 order by b.id desc")
    List<Booking> findUserBookingsWithEndIsBefore(Long userId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2")
    List<Booking> findOwnerBookingsByState(Long ownerId, BookingStatus state);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2")
    List<Booking> findUserBookingsByState(Long userId, BookingStatus state);

    @Query("select b from Booking b where b.booker.id = ?1 order by b.id desc")
    List<Booking> findUserBookings(Long bookerId);

    @Query("select b from Booking b where b.item.owner.id = ?1 order by b.id desc")
    List<Booking> findOwnerBookings(Long ownerId);

    @Query("select b from Booking b where b.item.id = ?1 and b.item.owner.id = ?2 and b.end < ?3")
    List<Booking> findLastOwnerBooking(Long itemId, Long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.id = ?1 and b.item.owner.id = ?2 and b.start > ?3")
    List<Booking> findNextOwnerBooking(Long itemId, Long ownerId, LocalDateTime now);

    @Query("select count(b) from Booking b where b.booker.id = ?1 and b.end < ?2 group by b.id")
    Long getCountOfUserBookingsWithEndIsBefore(Long userId, LocalDateTime now);
}
