package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findItemsByOwner_IdOrderByIdAsc(Long userId);

    @Query("select i from Item i" +
            " where (lower(i.name) like %:word%" +
            " or lower(i.description) like %:word%)" +
            " and i.available = true")
    List<Item> findItemsByNameOrDescription(@Param("word") String word);
}
