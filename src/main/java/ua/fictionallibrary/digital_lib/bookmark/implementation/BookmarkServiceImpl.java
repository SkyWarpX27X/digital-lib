package ua.fictionallibrary.digital_lib.bookmark.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.bookmark.*;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;

import java.util.UUID;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository repository;
    private final ApplicationEventPublisher publisher;

    public BookmarkServiceImpl(BookmarkRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public BookmarkResponse addBookmark(Bookmark bookmark) {
        if (repository.existsById(toId(bookmark.getUserId(), bookmark.getBookId())))
            throw new DuplicateException("User " + bookmark.getUserId() + "'s bookmark for book "
                    + bookmark.getBookId() + " already exists." );
        publisher.publishEvent(new BookmarkAddedEvent(
                bookmark.getUserId(),
                bookmark.getBookId(),
                bookmark.getPageNumber()
        ));
        return toResponse(repository.save(bookmark));
    }

    @Override
    public BookmarkResponse getBookmark(UUID userId, UUID bookId) {
        return repository.findById(toId(userId, bookId))
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("User " + userId + "'s bookmark for book "
                        + bookId + " does not exist."));
    }

    @Override
    public void deleteBookmark(UUID userId, UUID bookId) {
        if (!repository.existsById(toId(userId, bookId)))
            throw new DataNotFoundException("Failed to delete, not found user " + userId + "'s bookmark for book "
            + bookId + ".");
        repository.deleteById(toId(userId, bookId));
    }

    @Override
    public BookmarkResponse updatePageNumber(UUID userId, UUID bookId, UpdatePageNumberCommand command) {
        if (!repository.existsById(toId(userId, bookId)))
            throw new DataNotFoundException("Failed to update, not found user " + userId + "'s bookmark for book "
                    + bookId + ".");
        Bookmark updatedBookmark = new Bookmark(userId, bookId, command.pageNumber());
        return toResponse(repository.save(updatedBookmark));
    }

    private BookmarkResponse toResponse(Bookmark bookmark) {
        return new BookmarkResponse(bookmark.getUserId(), bookmark.getBookId(), bookmark.getPageNumber());
    }

    private BookmarkId toId(UUID userId, UUID bookId) {
        return new BookmarkId(userId, bookId);
    }
}
