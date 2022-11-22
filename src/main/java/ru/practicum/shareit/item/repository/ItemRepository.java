package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findItemsByOwnerIdOrderByIdAsc(Long userId, Pageable pageable);

    List<Item> findItemsByRequestId(Long requestId);

    @Query("select count(i) from Item i where i.owner.id=?1")
    Integer findCountOfUserItems(Long userId);

    @Query("select i from Item i" +
            " where (lower(i.name) like %:word%" +
            " or lower(i.description) like %:word%)" +
            " and i.available = true")
    List<Item> findItemsByNameOrDescription(@Param("word") String word, Pageable pageable);
}
