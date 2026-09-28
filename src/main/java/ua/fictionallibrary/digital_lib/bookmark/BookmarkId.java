package ua.fictionallibrary.digital_lib.bookmark;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class BookmarkId implements Serializable {

    private UUID userId;
    private UUID bookId;

    public BookmarkId() {
    }

    public BookmarkId(UUID userId, UUID bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUser(UUID userId) {
        this.userId = userId;
    }

    public UUID getBook() {
        return bookId;
    }

    public void setBook(UUID bookId) {
        this.bookId = bookId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookmarkId that = (BookmarkId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, bookId);
    }
}