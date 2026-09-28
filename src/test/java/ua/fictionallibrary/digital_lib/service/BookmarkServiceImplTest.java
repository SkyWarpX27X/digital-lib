package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.bookmark.*;
import ua.fictionallibrary.digital_lib.bookmark.implementation.BookmarkServiceImpl;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookmarkServiceImplTest {
    @Mock
    BookmarkRepository repository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    BookmarkService service;

    @BeforeEach
    void setUp() {
        service = new BookmarkServiceImpl(repository, eventPublisher);
    }

    private Bookmark entity() {
        return new Bookmark(UUID.randomUUID(), UUID.randomUUID(), 42);
    }

    private BookmarkId toId(UUID userId, UUID bookId) {
        return new BookmarkId(userId, bookId);
    }

    @Test
    void addBookmark_successfullyAddsAndPublishesEvent() {
        Bookmark bookmark = entity();
        when(repository.existsById(toId(bookmark.getUserId(), bookmark.getBookId()))).thenReturn(false);
        when(repository.save(bookmark)).thenReturn(bookmark);

        BookmarkResponse response = service.addBookmark(bookmark);

        assertNotNull(response);
        assertEquals(bookmark.getUserId(), response.userId());
        assertEquals(bookmark.getBookId(), response.bookId());
        assertEquals(bookmark.getPageNumber(), response.pageNumber());

        verify(repository).save(bookmark);

        ArgumentCaptor<BookmarkAddedEvent> eventCaptor = ArgumentCaptor.forClass(BookmarkAddedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(bookmark.getUserId(), eventCaptor.getValue().userId());
        assertEquals(bookmark.getBookId(), eventCaptor.getValue().bookId());
        assertEquals(bookmark.getPageNumber(), eventCaptor.getValue().pageNumber());
    }

    @Test
    void addBookmark_throwsOnDuplicate() {
        Bookmark bookmark = entity();
        when(repository.existsById(toId(bookmark.getUserId(), bookmark.getBookId()))).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addBookmark(bookmark));
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getBookmark_returnsCorrectDTO() {
        Bookmark bookmark = entity();
        when(repository.findById(toId(bookmark.getUserId(), bookmark.getBookId()))).thenReturn(Optional.of(bookmark));

        BookmarkResponse response = service.getBookmark(bookmark.getUserId(), bookmark.getBookId());

        assertNotNull(response);
        assertEquals(bookmark.getUserId(), response.userId());
        assertEquals(bookmark.getBookId(), response.bookId());
        assertEquals(bookmark.getPageNumber(), response.pageNumber());

        verify(repository).findById(toId(bookmark.getUserId(), bookmark.getBookId()));
    }

    @Test
    void getBookmark_throwsOnUnknownBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(repository.findById(toId(userId, bookId))).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getBookmark(userId, bookId));

        verify(repository).findById(toId(userId, bookId));
    }

    @Test
    void deleteBookmark_successfullyDeletesBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(repository.existsById(toId(userId, bookId))).thenReturn(true);

        service.deleteBookmark(userId, bookId);

        verify(repository).existsById(toId(userId, bookId));
        verify(repository).deleteById(toId(userId, bookId));
    }

    @Test
    void deleteBookmark_throwsOnUnknownBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(repository.existsById(toId(userId, bookId))).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.deleteBookmark(userId, bookId));

        verify(repository, never()).deleteById(any());
    }

    @Test
    void updatePageNumber_successfullyUpdates() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UpdatePageNumberCommand command = new UpdatePageNumberCommand(120);
        Bookmark updated = new Bookmark(userId, bookId, command.pageNumber());

        when(repository.existsById(toId(userId, bookId))).thenReturn(true);
        when(repository.save(any(Bookmark.class))).thenReturn(updated);

        BookmarkResponse response = service.updatePageNumber(userId, bookId, command);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(bookId, response.bookId());
        assertEquals(120, response.pageNumber());

        verify(repository).existsById(toId(userId, bookId));
        verify(repository).save(any(Bookmark.class));
    }

    @Test
    void updatePageNumber_throwsOnUnknownBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UpdatePageNumberCommand command = new UpdatePageNumberCommand(120);
        when(repository.existsById(toId(userId, bookId))).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.updatePageNumber(userId, bookId, command));

        verify(repository, never()).save(any());
    }


}
