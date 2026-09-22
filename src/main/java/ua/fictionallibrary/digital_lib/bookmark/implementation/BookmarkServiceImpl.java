package ua.fictionallibrary.digital_lib.bookmark.implementation;

import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkRepository;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkService;
import ua.fictionallibrary.digital_lib.bookmark.UpdatePageNumberCommand;
import ua.fictionallibrary.digital_lib.bookmark.model.BookmarkEntity;
import ua.fictionallibrary.digital_lib.bookmark.model.dto.BookmarkResponse;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;

import java.util.UUID;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository repository;

    public BookmarkServiceImpl(BookmarkRepository repository) {
        this.repository = repository;
    }

    @Override
    public BookmarkResponse addBookmark(BookmarkEntity bookmarkEntity) {
        if (repository.exists(bookmarkEntity.userId(), bookmarkEntity.bookId()))
            throw new DuplicateException("User " + bookmarkEntity.userId() + "'s bookmark for book "
                    + bookmarkEntity.bookId() + " already exists." );
        return toResponse(repository.saveBookmark(bookmarkEntity));
    }

    @Override
    public BookmarkResponse getBookmark(UUID userId, UUID bookId) {
        return repository.getBookmark(userId, bookId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("User " + userId + "'s bookmark for book "
                        + bookId + " does not exist."));
    }

    @Override
    public void deleteBookmark(UUID userId, UUID bookId) {
        if (!repository.exists(userId, bookId))
            throw new DataNotFoundException("Failed to delete, not found user " + userId + "'s bookmark for book "
            + bookId + ".");
        repository.deleteBookmark(userId, bookId);
    }

    @Override
    public BookmarkResponse updatePageNumber(UUID userId, UUID bookId, UpdatePageNumberCommand command) {
        if (!repository.exists(userId, bookId))
            throw new DataNotFoundException("Failed to update, not found user " + userId + "'s bookmark for book "
                    + bookId + ".");
        BookmarkEntity updatedBookmark = new BookmarkEntity(userId, bookId, command.pageNumber());
        return toResponse(repository.saveBookmark(updatedBookmark));
    }

    private BookmarkResponse toResponse(BookmarkEntity entity) {
        return new BookmarkResponse(entity);
    }
}
