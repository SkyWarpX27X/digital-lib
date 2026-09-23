package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkAddedEvent;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkRepository;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkService;
import ua.fictionallibrary.digital_lib.bookmark.UpdatePageNumberCommand;
import ua.fictionallibrary.digital_lib.bookmark.implementation.BookmarkServiceImpl;
import ua.fictionallibrary.digital_lib.bookmark.model.BookmarkEntity;
import ua.fictionallibrary.digital_lib.bookmark.model.dto.BookmarkResponse;
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

    private BookmarkEntity entity() {
        return new BookmarkEntity(UUID.randomUUID(), UUID.randomUUID(), 42);
    }

    @Test
    void addBookmark_successfullyAddsAndPublishesEvent() {
        BookmarkEntity bookmark = entity();
        when(repository.exists(bookmark.userId(), bookmark.bookId())).thenReturn(false);
        when(repository.saveBookmark(bookmark)).thenReturn(bookmark);

        BookmarkResponse response = service.addBookmark(bookmark);

        assertNotNull(response);
        assertEquals(bookmark.userId(), response.userId());
        assertEquals(bookmark.bookId(), response.bookId());
        assertEquals(bookmark.pageNumber(), response.pageNumber());

        verify(repository).saveBookmark(bookmark);

        ArgumentCaptor<BookmarkAddedEvent> eventCaptor = ArgumentCaptor.forClass(BookmarkAddedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(bookmark.userId(), eventCaptor.getValue().userId());
        assertEquals(bookmark.bookId(), eventCaptor.getValue().bookId());
        assertEquals(bookmark.pageNumber(), eventCaptor.getValue().pageNumber());
    }

    @Test
    void addBookmark_throwsOnDuplicate() {
        BookmarkEntity bookmark = entity();
        when(repository.exists(bookmark.userId(), bookmark.bookId())).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addBookmark(bookmark));
        verify(repository, never()).saveBookmark(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getBookmark_returnsCorrectDTO() {
        BookmarkEntity bookmark = entity();
        when(repository.getBookmark(bookmark.userId(), bookmark.bookId())).thenReturn(Optional.of(bookmark));

        BookmarkResponse response = service.getBookmark(bookmark.userId(), bookmark.bookId());

        assertNotNull(response);
        assertEquals(bookmark.userId(), response.userId());
        assertEquals(bookmark.bookId(), response.bookId());
        assertEquals(bookmark.pageNumber(), response.pageNumber());

        verify(repository).getBookmark(bookmark.userId(), bookmark.bookId());
    }

    @Test
    void getBookmark_throwsOnUnknownBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(repository.getBookmark(userId, bookId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getBookmark(userId, bookId));

        verify(repository).getBookmark(userId, bookId);
    }

    @Test
    void deleteBookmark_successfullyDeletesBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(repository.exists(userId, bookId)).thenReturn(true);

        service.deleteBookmark(userId, bookId);

        verify(repository).exists(userId, bookId);
        verify(repository).deleteBookmark(userId, bookId);
    }

    @Test
    void deleteBookmark_throwsOnUnknownBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(repository.exists(userId, bookId)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.deleteBookmark(userId, bookId));

        verify(repository, never()).deleteBookmark(any(), any());
    }

    @Test
    void updatePageNumber_successfullyUpdates() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UpdatePageNumberCommand command = new UpdatePageNumberCommand(120);
        BookmarkEntity updated = new BookmarkEntity(userId, bookId, command.pageNumber());

        when(repository.exists(userId, bookId)).thenReturn(true);
        when(repository.saveBookmark(updated)).thenReturn(updated);

        BookmarkResponse response = service.updatePageNumber(userId, bookId, command);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(bookId, response.bookId());
        assertEquals(120, response.pageNumber());

        verify(repository).exists(userId, bookId);
        verify(repository).saveBookmark(updated);
    }

    @Test
    void updatePageNumber_throwsOnUnknownBookmark() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UpdatePageNumberCommand command = new UpdatePageNumberCommand(120);
        when(repository.exists(userId, bookId)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.updatePageNumber(userId, bookId, command));

        verify(repository, never()).saveBookmark(any());
    }


}
