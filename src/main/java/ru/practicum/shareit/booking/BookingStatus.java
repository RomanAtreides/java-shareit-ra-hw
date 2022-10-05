package ru.practicum.shareit.booking;

public enum BookingStatus {
    ALL,        // Все бронирования
    FUTURE,     // Будущие
    CURRENT,    // Текущие
    PAST,       // Завершённые
    WAITING,    // Новое бронирование, ожидает одобрения
    APPROVED,   // Бронирование подтверждено владельцем
    REJECTED,   // Отклонено владельцем
    CANCELED;   // Отменено создателем

    public static BookingStatus from(String state) {
        for (BookingStatus value : BookingStatus.values()) {
            if (value.name().equals(state)) {
                return value;
            }
        }
        return null;
    }
}
