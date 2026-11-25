package ua.fictionallibrary.digital_lib.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.bookId = :bookId")
    void deleteByBookId(UUID bookId);
}
