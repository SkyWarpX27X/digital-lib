package ua.fictionallibrary.digital_lib.favorite;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class Favorite {
    @Id
    private UUID userId;
    @Id
    private UUID bookId;

    protected Favorite() {}
    public Favorite(UUID userId, UUID bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getBookId() {
        return bookId;
    }
}
