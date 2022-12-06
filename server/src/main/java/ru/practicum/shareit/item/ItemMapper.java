package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class ItemMapper {

    public static Item toItem(ItemDto itemDto, User owner, ItemRequest itemRequest) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(itemRequest)
                .build();
    }

    public static Item toItemFromInfoDto(ItemInfoDto infoDto, User owner, ItemRequest itemRequest) {
        return Item.builder()
                .id(infoDto.getId())
                .name(infoDto.getName())
                .description(infoDto.getDescription())
                .available(infoDto.getAvailable())
                .owner(owner)
                .request(itemRequest)
                .build();
    }

    public static Comment toComment(CommentShortDto commentShortDto, Item item, User user) {
        return Comment.builder()
                .id(commentShortDto.getId())
                .text(commentShortDto.getText())
                .item(item)
                .author(user)
                .created(commentShortDto.getCreated())
                .build();
    }

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment, User user) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                user.getName(),
                comment.getCreated()
        );
    }

    public static ItemInfoDto toItemInfoDto(
            Item item,
            Booking lastBooking,
            Booking nextBooking,
            List<CommentDto> commentDtoRegister) {
        ItemInfoDto.BookingForItemDto lastBookingToAdd = null;
        ItemInfoDto.BookingForItemDto nextBookingToAdd = null;

        if (lastBooking != null) {
            lastBookingToAdd = new ItemInfoDto.BookingForItemDto(
                    lastBooking.getId(),
                    lastBooking.getStart(),
                    lastBooking.getEnd(),
                    lastBooking.getBooker().getId()
            );
        }

        if (nextBooking != null) {
            nextBookingToAdd = new ItemInfoDto.BookingForItemDto(
                    nextBooking.getId(),
                    nextBooking.getStart(),
                    nextBooking.getEnd(),
                    nextBooking.getBooker().getId()
            );
        }

        return ItemInfoDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBookingToAdd)
                .nextBooking(nextBookingToAdd)
                .comments(commentDtoRegister)
                .request(item.getRequest())
                .build();
    }
}
