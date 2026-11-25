package ua.fictionallibrary.digital_lib.bookmark.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.bookmark.*;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.starter.exception.DuplicateException;

import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository repository;
    private final ApplicationEventPublisher publisher;
    private final DigitizedBookService digitizedBookService;

    public BookmarkServiceImpl(BookmarkRepository repository, ApplicationEventPublisher publisher,
                               DigitizedBookService digitizedBookService) {
        this.repository = repository;
        this.publisher = publisher;
        this.digitizedBookService = digitizedBookService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getUserBookmarks(UUID userId) {
        List<Bookmark> bookmarks = repository.findByUserId(userId);
        Map<UUID, String> names = digitizedBookService
                .getDigitizedBooks(bookmarks.stream().map(Bookmark::getBookId).toList(), false).stream()
                .collect(Collectors.toMap(DigitizedBookResponse::id, DigitizedBookResponse::name));
        return bookmarks.stream()
                .filter(b -> names.containsKey(b.getBookId()))
                .map(b -> new BookmarkResponse(b.getUserId(), b.getBookId(), b.getPageNumber(), names.get(b.getBookId())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteBookmarksForBook(UUID bookId) {
        repository.deleteByBookId(bookId);
    }

    @Override
    @Transactional
    public BookmarkResponse addBookmark(Bookmark bookmark) {
        if (repository.existsById(toId(bookmark.getUserId(), bookmark.getBookId())))
            throw new DuplicateException("User " + bookmark.getUserId() + "'s bookmark for book "
                    + bookmark.getBookId() + " already exists." );
        Bookmark saved = repository.save(bookmark);
        publisher.publishEvent(new BookmarkAddedEvent(
                saved.getUserId(),
                saved.getBookId(),
                saved.getPageNumber()
        ));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkResponse getBookmark(UUID userId, UUID bookId) {
        return repository.findById(toId(userId, bookId))
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("User " + userId + "'s bookmark for book "
                        + bookId + " does not exist."));
    }

    @Override
    @Transactional
    public void deleteBookmark(UUID userId, UUID bookId) {
        if (!repository.existsById(toId(userId, bookId)))
            throw new DataNotFoundException("Failed to delete, not found user " + userId + "'s bookmark for book "
                    + bookId + ".");
        repository.deleteById(toId(userId, bookId));
    }

    @Override
    @Transactional
    public BookmarkResponse updatePageNumber(UUID userId, UUID bookId, UpdatePageNumberCommand command) {
        Bookmark bookmark = repository.findById(toId(userId, bookId))
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found user " + userId + "'s bookmark for book "
                        + bookId + "."));
        bookmark.setPageNumber(command.pageNumber());
        return toResponse(repository.save(bookmark));
    }

    private BookmarkResponse toResponse(Bookmark bookmark) {
        return new BookmarkResponse(bookmark.getUserId(), bookmark.getBookId(), bookmark.getPageNumber(), null);
    }

    private BookmarkId toId(UUID userId, UUID bookId) {
        return new BookmarkId(userId, bookId);
    }
}
