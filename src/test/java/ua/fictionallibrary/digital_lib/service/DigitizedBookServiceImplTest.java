package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorService;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.implementations.DigitizedBookServiceImpl;
import ua.fictionallibrary.digital_lib.exception.InvalidUploadException;
import ua.fictionallibrary.digital_lib.filestorage.FileStorageService;
import ua.fictionallibrary.digital_lib.filestorage.FileValidator;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DigitizedBookServiceImplTest {
    @Mock
    DigitizedBookRepository repository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    AuthorService authorService;
    @Mock
    FileValidator fileValidator;
    @Mock
    FileStorageService fileStorageService;

    DigitizedBookServiceImpl service;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        service = new DigitizedBookServiceImpl(repository, eventPublisher, authorService, fileValidator, fileStorageService);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.clearSynchronization();
    }

    private DigitizedBookRequest request() {
        return new DigitizedBookRequest("King Arthur in Cornwall", List.of("Dickinson W. Howship"),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false,
                UUID.randomUUID());
    }

    private DigitizedBook entity() {
        return new DigitizedBook("King Arthur in Cornwall", Set.of(new Author("Dickinson W. Howship")),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false);
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile("pdf", "book.pdf", "application/pdf", "%PDF-1.4 test".getBytes());
    }

    private MockMultipartFile coverFile() {
        return new MockMultipartFile("cover", "cover.png", "image/png", new byte[]{1, 2, 3});
    }

    private void fireSynchronizations(int status) {
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(status));
    }

    private void fireCommit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> {
            s.afterCommit();
            s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        });
    }

    @Test
    void addDigitizedBook_successfullyAddsAndPublishesEvent() {
        DigitizedBookRequest request = request();
        MockMultipartFile pdf = pdfFile();
        MockMultipartFile cover = coverFile();
        when(fileValidator.validateCoverImage(cover)).thenReturn("image/png");
        when(authorService.findOrCreate(request.authors())).thenReturn(Set.of(new Author("Dickinson W. Howship")));
        when(repository.save(any(DigitizedBook.class))).thenAnswer(inv -> inv.getArgument(0));

        DigitizedBookResponse response = service.addDigitizedBook(request, request.physicalBookId(), pdf, cover);
        assertNotNull(response);
        assertEquals("King Arthur in Cornwall", response.name());
        assertEquals(List.of("Dickinson W. Howship"), response.authors());
        assertEquals("The book explores the existence and life of King Arthur", response.description());
        assertEquals("Історична", response.topic());
        assertEquals(Year.of(1850), response.publishingYear());
        assertEquals("eng", response.language());
        assertEquals("Стародрук", response.resourceType());
        assertFalse(response.isCopyrighted());
        assertEquals("/api/v1/digitized-books/" + response.id() + "/cover", response.coverUrl());
        assertEquals("/api/v1/digitized-books/" + response.id() + "/pdf", response.pdfUrl());

        verify(fileValidator).validatePdf(pdf);
        verify(fileValidator).validateCoverImage(cover);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileStorageService).store(keyCaptor.capture(), any(), anyLong(), eq("application/pdf"));
        String pdfKey = keyCaptor.getValue();
        assertTrue(pdfKey.startsWith("digitized-books/"));
        assertTrue(pdfKey.endsWith("/book.pdf"));

        verify(fileStorageService).store(keyCaptor.capture(), any(), anyLong(), eq("image/png"));
        String coverKey = keyCaptor.getValue();
        assertTrue(coverKey.endsWith("/cover.png"));
        assertEquals(pdfKey.substring(0, pdfKey.lastIndexOf('/')),
                coverKey.substring(0, coverKey.lastIndexOf('/')));

        ArgumentCaptor<DigitizedBook> savedCaptor = ArgumentCaptor.forClass(DigitizedBook.class);
        verify(repository).save(savedCaptor.capture());
        assertEquals(pdfKey, savedCaptor.getValue().getPdfKey());
        assertEquals(coverKey, savedCaptor.getValue().getCoverKey());
        assertEquals("image/png", savedCaptor.getValue().getCoverContentType());

        ArgumentCaptor<BookDigitizedEvent> eventCaptor = ArgumentCaptor.forClass(BookDigitizedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(response.id(), eventCaptor.getValue().digitizedId());
        assertEquals(request.physicalBookId(), eventCaptor.getValue().physicalId());

        fireSynchronizations(TransactionSynchronization.STATUS_COMMITTED);
        verify(fileStorageService, never()).delete(anyString());
    }

    @Test
    void addDigitizedBook_withoutCover_uploadsOnlyPdfAndHasNoCoverUrl() {
        DigitizedBookRequest request = request();
        when(repository.save(any(DigitizedBook.class))).thenAnswer(inv -> inv.getArgument(0));

        DigitizedBookResponse response = service.addDigitizedBook(request, request.physicalBookId(), pdfFile(), null);

        assertNull(response.coverUrl());
        assertEquals("/api/v1/digitized-books/" + response.id() + "/pdf", response.pdfUrl());
        verify(fileValidator, never()).validateCoverImage(any());
        verify(fileStorageService, times(1)).store(anyString(), any(), anyLong(), eq("application/pdf"));
        verify(fileStorageService, times(1)).store(anyString(), any(), anyLong(), anyString());

        ArgumentCaptor<DigitizedBook> savedCaptor = ArgumentCaptor.forClass(DigitizedBook.class);
        verify(repository).save(savedCaptor.capture());
        assertNull(savedCaptor.getValue().getCoverKey());
        assertNull(savedCaptor.getValue().getCoverContentType());
    }

    @Test
    void addDigitizedBook_invalidPdf_doesNotUploadOrSave() {
        DigitizedBookRequest request = request();
        MockMultipartFile pdf = pdfFile();
        doThrow(new InvalidUploadException("File is not a valid PDF")).when(fileValidator).validatePdf(pdf);

        assertThrows(InvalidUploadException.class,
                () -> service.addDigitizedBook(request, request.physicalBookId(), pdf, null));

        verifyNoInteractions(fileStorageService, repository, eventPublisher);
    }

    @Test
    void addDigitizedBook_whenSaveFailsAndTransactionRollsBack_deletesUploadedFiles() {
        DigitizedBookRequest request = request();
        MockMultipartFile cover = coverFile();
        when(fileValidator.validateCoverImage(cover)).thenReturn("image/png");
        when(repository.save(any(DigitizedBook.class))).thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class,
                () -> service.addDigitizedBook(request, request.physicalBookId(), pdfFile(), cover));

        ArgumentCaptor<String> uploadedKeys = ArgumentCaptor.forClass(String.class);
        verify(fileStorageService, times(2)).store(uploadedKeys.capture(), any(), anyLong(), anyString());
        verify(eventPublisher, never()).publishEvent(any());

        fireSynchronizations(TransactionSynchronization.STATUS_ROLLED_BACK);

        for (String key : uploadedKeys.getAllValues()) {
            verify(fileStorageService).delete(key);
        }
    }

    @Test
    void getDigitizedBook_returnsCorrectDTO(){
        DigitizedBook book = entity();
        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.getId(), false);
        assertNotNull(response);
        assertEquals(book.getId(), response.id());
        assertEquals("King Arthur in Cornwall", response.name());
        assertEquals(List.of("Dickinson W. Howship"), response.authors());
        assertEquals("The book explores the existence and life of King Arthur", response.description());
        assertEquals("Історична", response.topic());
        assertEquals(Year.of(1850), response.publishingYear());
        assertEquals("eng", response.language());
        assertEquals("Стародрук", response.resourceType());
        assertFalse(response.isCopyrighted());
        assertNull(response.coverUrl());
        assertEquals("/api/v1/digitized-books/" + book.getId() + "/pdf", response.pdfUrl());

        verify(repository).findById(book.getId());
    }

    @Test
    void getDigitizedBook_withCover_returnsCoverUrl() {
        DigitizedBook book = entity();
        book.setCoverKey("digitized-books/abc/cover.jpg");
        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.getId(), false);

        assertEquals("/api/v1/digitized-books/" + book.getId() + "/cover", response.coverUrl());
    }

    @Test
    void getDigitizedBook_copyrightedWithoutAccess_hidesPdf() {
        DigitizedBook book = entity();
        book.setCopyrighted(true);
        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.getId(), false);

        assertFalse(response.canRead());
        assertNull(response.pdfUrl());
        assertEquals(book.getName(), response.name());
    }

    @Test
    void getDigitizedBook_copyrightedWithAccess_returnsPdf() {
        DigitizedBook book = entity();
        book.setCopyrighted(true);
        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.getId(), true);

        assertTrue(response.canRead());
        assertEquals("/api/v1/digitized-books/" + book.getId() + "/pdf", response.pdfUrl());
    }

    @Test
    void getDigitizedBook_freeBookWithoutAccess_returnsPdf() {
        DigitizedBook book = entity();
        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.getId(), false);

        assertTrue(response.canRead());
        assertNotNull(response.pdfUrl());
    }

    @Test
    void getDigitizedBook_throwsOnUnknownBook(){
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getDigitizedBook(UUID.randomUUID(), false));

        verify(repository).findById(any());
    }

    @Test
    void getAllDigitizedBooks_returnsAllBooks() {
        DigitizedBook book1 = entity();
        DigitizedBook book2 = entity();
        when(repository.findAll()).thenReturn(List.of(book1, book2));

        List<DigitizedBookResponse> responses = service.getAllDigitizedBooks(false);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(book1.getId(), responses.get(0).id());
        assertEquals(book2.getId(), responses.get(1).id());
        assertEquals(book1.getName(), responses.get(0).name());
        assertEquals(book2.getName(), responses.get(1).name());

        verify(repository).findAll();
    }

    @Test
    void getAllDigitizedBooks_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<DigitizedBookResponse> responses = service.getAllDigitizedBooks(false);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(repository).findAll();
    }

    @Test
    void updateDigitizedBook_throwsOnUnknownBook() {
        DigitizedBook book = entity();
        DigitizedBookRequest request = request();
        when(repository.findById(book.getId())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.updateDigitizedBook(book.getId(), request));

        verify(repository, never()).save(any());
    }

    @Test
    void deleteDigitizedBook_deletesRowAndFiles() {
        UUID id = UUID.randomUUID();
        DigitizedBook book = entity();
        book.setPdfKey("digitized-books/abc/book.pdf");
        book.setCoverKey("digitized-books/abc/cover.png");
        when(repository.findById(id)).thenReturn(Optional.of(book));

        service.deleteDigitizedBook(id);
        fireCommit();

        verify(repository).deleteById(id);
        verify(fileStorageService).delete("digitized-books/abc/book.pdf");
        verify(fileStorageService).delete("digitized-books/abc/cover.png");
    }

    @Test
    void deleteDigitizedBook_withoutCover_deletesOnlyPdf() {
        UUID id = UUID.randomUUID();
        DigitizedBook book = entity();
        book.setPdfKey("digitized-books/abc/book.pdf");
        when(repository.findById(id)).thenReturn(Optional.of(book));

        service.deleteDigitizedBook(id);
        fireCommit();

        verify(fileStorageService, times(1)).delete(anyString());
        verify(fileStorageService).delete("digitized-books/abc/book.pdf");
    }

    @Test
    void deleteDigitizedBook_whenRowDeleteFails_keepsFiles() {
        UUID id = UUID.randomUUID();
        DigitizedBook book = entity();
        book.setPdfKey("digitized-books/abc/book.pdf");
        when(repository.findById(id)).thenReturn(Optional.of(book));
        doThrow(new RuntimeException("db down")).when(repository).deleteById(id);

        assertThrows(RuntimeException.class, () -> service.deleteDigitizedBook(id));
        fireSynchronizations(TransactionSynchronization.STATUS_ROLLED_BACK);

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void deleteDigitizedBook_throwsOnUnknownBook() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.deleteDigitizedBook(id));

        verify(repository, never()).delete(any(DigitizedBook.class));
        verify(repository, never()).deleteById(any());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void exists_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        assertTrue(service.exists(id));
        verify(repository).existsById(id);
    }

}
