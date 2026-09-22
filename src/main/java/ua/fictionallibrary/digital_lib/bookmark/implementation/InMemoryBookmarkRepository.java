package ua.fictionallibrary.digital_lib.bookmark.implementation;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkRepository;
import ua.fictionallibrary.digital_lib.bookmark.model.BookmarkEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookmarkRepository implements BookmarkRepository {
    private record PrimaryKey(UUID userId, UUID bookId) {}
    private final Map<PrimaryKey, BookmarkEntity> bookmarks = new ConcurrentHashMap<>();

    @Override
    public BookmarkEntity saveBookmark(BookmarkEntity bookmark) {
        bookmarks.put(new PrimaryKey(bookmark.userId(), bookmark.bookId()), bookmark);
        return bookmark;
    }

    @Override
    public Optional<BookmarkEntity> getBookmark(UUID userId, UUID bookId) {
        return Optional.ofNullable(bookmarks.get(new PrimaryKey(userId, bookId)));
    }

    @Override
    public void deleteBookmark(UUID userId, UUID bookId) {
        bookmarks.remove(new PrimaryKey(userId, bookId));
    }

    @Override
    public boolean exists(UUID userId, UUID bookId) {
        return bookmarks.containsKey(new PrimaryKey(userId, bookId));
    }
}
