package ua.fictionallibrary.digital_lib.favorite;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class FavoriteId implements Serializable {
    private UUID userId;
    private UUID bookId;

    public FavoriteId() {
    }

    public FavoriteId(UUID userId, UUID bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteId that = (FavoriteId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, bookId);
    }
}
