package ua.fictionallibrary.digital_lib.bookmark;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "bookmarks")
@IdClass(BookmarkId.class)
public class Bookmark {
    @Id
    private UUID userId;
    @Id
    private UUID bookId;
    @Column
    private int pageNumber;

    protected Bookmark() {}
    public Bookmark(UUID userId, UUID bookId, int pageNumber) {
        this.userId = userId;
        this.bookId = bookId;
        this.pageNumber = pageNumber;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}
