package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.exception.LibraryCardNotFoundException;
import ua.fictionallibrary.digital_lib.exception.SelfDeactivationException;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.starter.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.user.LibraryCardService;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRole;
import ua.fictionallibrary.digital_lib.user.UserService;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.dto.UserResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class AuditLogIT extends AbstractFlowIT {

    @Autowired
    PhysicalBookService physicalBookService;
    @Autowired
    DigitizedBookService digitizedBookService;
    @Autowired
    BookOrderService bookOrderService;
    @Autowired
    UserService userService;
    @Autowired
    LibraryCardService libraryCardService;

    @BeforeEach
    void clean() {
        List<String> tables = jdbc.sql("""
                SELECT tablename FROM pg_tables
                WHERE schemaname = 'public'
                  AND tablename NOT IN ('flyway_schema_history', 'databasechangelog', 'databasechangeloglock')
                """).query(String.class).list();
        if (!tables.isEmpty())
            jdbc.sql("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE").update();
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addDigitizedBook_auditsSuccessfully() throws IOException {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        UUID physicalBookId = UUID.randomUUID();
        DigitizedBookRequest book = digitizedBookRequest("King Arthur in Cornwall", physicalBookId);
        DigitizedBookResponse response = digitizedBookService.addDigitizedBook(book, physicalBookId,
                TestFiles.sampleMultipartPdf(), null);
        awaitDigitizationCascade(physicalBookId);
        assertThat(jdbc.sql("SELECT count(*) FROM digitized_books").query(Long.class).single()).isEqualTo(1);
        Map<String, Object> row = oneRowOf("DigitizedBookServiceImpl.addDigitizedBook(..)");
        assertSuccess(row, username, "DigitizedBookServiceImpl.addDigitizedBook(..)");
        assertThat((String) row.get("args")).contains("King Arthur in Cornwall");
        assertThat(row.get("result")).isEqualTo(response.toString());
    }

    @Test
    void updateDigitizedBook_auditsSuccessfully() throws IOException {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        DigitizedBookResponse book = saveDigitizedBook("Original title");
        DigitizedBookResponse updated = digitizedBookService.updateDigitizedBook(book.id(),
                digitizedBookRequest("Updated title", UUID.randomUUID()));
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "DigitizedBookServiceImpl.updateDigitizedBook(..)");
        assertThat((String) row.get("args")).contains(book.id().toString()).contains("Updated title");
        assertThat(row.get("result")).isEqualTo(updated.toString());
    }

    @Test
    void deleteDigitizedBook_auditsSuccessfully() throws IOException {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        DigitizedBookResponse book = saveDigitizedBook("Get rekt");
        digitizedBookService.deleteDigitizedBook(book.id());
        assertThat(digitizedBookService.exists(book.id())).isFalse();
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "DigitizedBookServiceImpl.deleteDigitizedBook(..)");
        assertThat(row.get("result")).isEqualTo("null");
    }

    @Test
    void addDigitizedBook_storageFailure_failedAuditRowSurvivesRollback() throws IOException {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        UUID physicalBookId = UUID.randomUUID();
        doThrow(new RuntimeException("s3 down")).when(fileStorageService).store(anyString(), any(), anyLong(), anyString());
        assertThrows(RuntimeException.class, () -> digitizedBookService.addDigitizedBook(digitizedBookRequest("It's so over",
                        physicalBookId), physicalBookId, TestFiles.sampleMultipartPdf(), null));

        assertThat(jdbc.sql("SELECT count(*) FROM digitized_books").query(Long.class).single()).isZero();
        Map<String, Object> row = oneRow();
        assertFailure(row, username, "DigitizedBookServiceImpl.addDigitizedBook(..)", RuntimeException.class);
        assertThat((String) row.get("result")).contains("s3 down");
    }

    @Test
    void updateDigitizedBook_notFound_auditsFailure() {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        assertThrows(DataNotFoundException.class, () -> digitizedBookService.updateDigitizedBook(UUID.randomUUID(),
                digitizedBookRequest("U can't see me", UUID.randomUUID())));
        assertFailure(oneRow(), username, "DigitizedBookServiceImpl.updateDigitizedBook(..)",
                DataNotFoundException.class);
    }

    @Test
    void deleteDigitizedBook_notFound_auditsFailure() {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        assertThrows(DataNotFoundException.class, () -> digitizedBookService.deleteDigitizedBook(UUID.randomUUID()));
        assertFailure(oneRow(), username, "DigitizedBookServiceImpl.deleteDigitizedBook(..)", DataNotFoundException.class);
    }

    @Test
    void addPhysicalBook_auditsSuccessfully() {
        String username = loginAs(UserRole.BOOK_DELIVERY_DEPARTMENT);
        PhysicalBookResponse response = physicalBookService.addPhysicalBook(physicalBookRequest("To Kill a Mockingbird"));
        assertThat(physicalBookService.exists(response.id())).isTrue();
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "PhysicalBookServiceImpl.addPhysicalBook(..)");
        assertThat((String) row.get("args")).contains("To Kill a Mockingbird");
        assertThat(row.get("result")).isEqualTo(response.toString());
    }

    @Test
    void updatePhysicalBook_auditsSuccessfully() {
        String username = loginAs(UserRole.BOOK_DELIVERY_DEPARTMENT);
        PhysicalBookResponse book = savePhysicalBook("Old name");
        PhysicalBookResponse updated = physicalBookService.updatePhysicalBook(book.id(), physicalBookRequest("New name"));
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "PhysicalBookServiceImpl.updatePhysicalBook(..)");
        assertThat((String) row.get("args")).contains(book.id().toString()).contains("New name");
        assertThat(row.get("result")).isEqualTo(updated.toString());
    }

    @Test
    void deletePhysicalBook_auditsSuccessfully() {
        String username = loginAs(UserRole.BOOK_DELIVERY_DEPARTMENT);
        PhysicalBookResponse book = savePhysicalBook("Get rekt");
        physicalBookService.deletePhysicalBook(book.id());
        assertThat(physicalBookService.exists(book.id())).isFalse();
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "PhysicalBookServiceImpl.deletePhysicalBook(..)");
        assertThat((String) row.get("args")).contains(book.id().toString());
        assertThat(row.get("result")).isEqualTo("null");
    }

    @Test
    void updatePhysicalBook_notFound_auditsFailure() {
        String username = loginAs(UserRole.BOOK_DELIVERY_DEPARTMENT);
        assertThrows(DataNotFoundException.class, () -> physicalBookService.updatePhysicalBook(UUID.randomUUID(),
                physicalBookRequest("\"It's not like I like you or anything... baka...\" The Novel")));
        assertFailure(oneRow(), username, "PhysicalBookServiceImpl.updatePhysicalBook(..)", DataNotFoundException.class);
    }

    @Test
    void deletePhysicalBook_notFound_auditsFailure() {
        String username = loginAs(UserRole.BOOK_DELIVERY_DEPARTMENT);
        assertThrows(DataNotFoundException.class, () -> physicalBookService.deletePhysicalBook(UUID.randomUUID()));
        assertFailure(oneRow(), username, "PhysicalBookServiceImpl.deletePhysicalBook(..)", DataNotFoundException.class);
    }

    @Test
    void addBookOrder_auditsSuccessfully() {
        String username = loginAs(UserRole.READER);
        UUID userId = idOf(username);
        saveLibraryCard(userId, "bababooey@example.com");
        PhysicalBookResponse book = savePhysicalBook("We still reading books?");
        BookOrderResponse response = bookOrderService.addBookOrder(new BookOrderRequest(
                "bababooey@example.com", book.id(), true), userId);
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "BookOrderServiceImpl.addBookOrder(..)");
        assertThat((String) row.get("args")).contains(book.id().toString()).contains(userId.toString());
        assertThat(row.get("result")).isEqualTo(response.toString());
    }

    @Test
    void updateStatus_auditsSuccessfully() {
        String reader = loginAs(UserRole.READER);
        UUID readerId = idOf(reader);
        saveLibraryCard(readerId, "bababooey@example.com");
        PhysicalBookResponse book = savePhysicalBook("We still reading books?");
        BookOrderResponse order = saveOpenOrder(readerId, book.id());
        String bdd = loginAs(UserRole.BOOK_DELIVERY_DEPARTMENT);
        BookOrderResponse closed = bookOrderService.updateStatus(order.id(), new UpdateStatusCommand(false));
        Map<String, Object> row = oneRow();
        assertSuccess(row, bdd, "BookOrderServiceImpl.updateStatus(..)");
        assertThat((String) row.get("args")).contains(order.id().toString());
        assertThat(row.get("result")).isEqualTo(closed.toString());
    }

    @Test
    void addBookOrder_withoutLibraryCard_auditsFailure() {
        String username = loginAs(UserRole.READER);
        UUID userId = idOf(username);
        PhysicalBookResponse book = savePhysicalBook("We still reading books?");
        assertThrows(LibraryCardNotFoundException.class, () -> bookOrderService.addBookOrder(new BookOrderRequest(
                "bababooey@example.com", book.id(), true), userId));
        assertFailure(oneRow(), username, "BookOrderServiceImpl.addBookOrder(..)", LibraryCardNotFoundException.class);
    }

    @Test
    void updateStatus_reopenClosedOrder_auditsFailure() {
        String username = loginAs(UserRole.READER);
        UUID userId = idOf(username);
        saveLibraryCard(userId, "bababooey@example.com");
        PhysicalBookResponse book = savePhysicalBook("We still reading books?");
        BookOrderResponse order = saveOpenOrder(userId, book.id());
        bookOrderService.updateStatus(order.id(), new UpdateStatusCommand(false));
        resetAudit();
        assertThrows(InvalidOrderUpdateException.class, () -> bookOrderService.updateStatus(order.id(),
                new UpdateStatusCommand(true)));
        assertFailure(oneRow(), username, "BookOrderServiceImpl.updateStatus(..)", InvalidOrderUpdateException.class);
    }

    @Test
    void addUser_auditsSuccessfully() {
        String admin = loginAs(UserRole.ADMIN);
        String newUsername = "new_user_" + UUID.randomUUID().toString().substring(0, 8);
        UserResponse response = userService.addUser(newUser(newUsername, "S3cretPwd!", UserRole.READER));
        assertThat(userService.existsByLogin(newUsername)).isTrue();
        Map<String, Object> row = oneRow();
        assertSuccess(row, admin, "UserServiceImpl.addUser(..)");
        assertThat((String) row.get("args")).contains(newUsername);
        assertThat(row.get("result")).isEqualTo(response.toString());
    }

    @Test
    void addUser_rawPasswordIsNeverStored() {
        loginAs(UserRole.ADMIN);
        String newUsername = "new_user_" + UUID.randomUUID().toString().substring(0, 8);
        userService.addUser(newUser(newUsername, "S3cretPwd!", UserRole.READER));
        Map<String, Object> row = oneRow();
        assertThat((String) row.get("args")).doesNotContain("S3cretPwd!");
        assertThat((String) row.get("result")).doesNotContain("S3cretPwd!");
    }

    @Test
    void updateUser_selfUpdate_auditsSuccessfully() {
        String username = loginAs(UserRole.READER);
        UUID userId = idOf(username);
        UserResponse response = userService.updateUser(userId, newUser(username, "NewPwd123!", UserRole.READER),
                userId, false);
        Map<String, Object> row = oneRow();
        assertSuccess(row, username, "UserServiceImpl.updateUser(..)");
        assertThat((String) row.get("args")).contains(userId.toString());
        assertThat(row.get("result")).isEqualTo(response.toString());
    }

    @Test
    void updateRoleAndStatus_auditsSuccessfully() {
        String admin = loginAs(UserRole.ADMIN);
        UUID adminId = idOf(admin);
        UUID targetId = idOf(createUser(UserRole.READER));
        UserResponse response = userService.updateRoleAndStatus(targetId, UserRole.BOOK_DELIVERY_DEPARTMENT, false, adminId);
        Map<String, Object> row = oneRow();
        assertSuccess(row, admin, "UserServiceImpl.updateRoleAndStatus(..)");
        assertThat((String) row.get("args")).contains(targetId.toString());
        assertThat(row.get("result")).isEqualTo(response.toString());
        assertThat(jdbc.sql("SELECT role FROM users WHERE id = :id").param("id", targetId)
                .query(String.class).single()).isEqualTo(UserRole.BOOK_DELIVERY_DEPARTMENT.name());
    }

    @Test
    void deleteUser_auditsSuccessfully() {
        String admin = loginAs(UserRole.ADMIN);
        UUID targetId = idOf(createUser(UserRole.READER));
        userService.deleteUser(targetId);
        assertThat(userService.existsById(targetId)).isFalse();
        Map<String, Object> row = oneRow();
        assertSuccess(row, admin, "UserServiceImpl.deleteUser(..)");
        assertThat((String) row.get("args")).contains(targetId.toString());
        assertThat(row.get("result")).isEqualTo("null");
    }

    @Test
    void addUser_duplicateUsername_auditsFailure() {
        String admin = loginAs(UserRole.ADMIN);
        String existing = createUser(UserRole.READER);
        assertThrows(DuplicateException.class, () -> userService.addUser(newUser(existing, "S3cretPwd!",
                UserRole.READER)));
        assertFailure(oneRow(), admin, "UserServiceImpl.addUser(..)", DuplicateException.class);
    }

    @Test
    void updateUser_notFound_auditsFailure() {
        String admin = loginAs(UserRole.ADMIN);
        UUID adminId = idOf(admin);
        assertThrows(DataNotFoundException.class, () -> userService.updateUser(UUID.randomUUID(),
                newUser("some_rando", "pwd", UserRole.READER), adminId, true));
        assertFailure(oneRow(), admin, "UserServiceImpl.updateUser(..)", DataNotFoundException.class);
    }

    @Test
    void updateRoleAndStatus_selfDeactivation_auditsFailure() {
        String admin = loginAs(UserRole.ADMIN);
        UUID adminId = idOf(admin);
        assertThrows(SelfDeactivationException.class, () -> userService.updateRoleAndStatus(adminId, null,
                false, adminId));
        assertFailure(oneRow(), admin, "UserServiceImpl.updateRoleAndStatus(..)", SelfDeactivationException.class);
    }

    @Test
    void deleteUser_notFound_auditsFailure() {
        String admin = loginAs(UserRole.ADMIN);

        assertThrows(DataNotFoundException.class, () -> userService.deleteUser(UUID.randomUUID()));

        assertFailure(oneRow(), admin, "UserServiceImpl.deleteUser(..)", DataNotFoundException.class);
    }

    @Test
    void noAuthentication_isAuditedAsSystem() {
        physicalBookService.addPhysicalBook(physicalBookRequest("I AM A HACKER HACKER MAN"));
        assertThat(oneRow().get("username")).isEqualTo("system");
    }

    @Test
    void anonymousAuthenticationToken_isAuditedAsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        physicalBookService.addPhysicalBook(physicalBookRequest("I AM A HACKER HACKER MAN"));
        assertThat(oneRow().get("username")).isEqualTo("anonymous");
    }

    @Test
    void unauthenticatedToken_isAuditedAsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated("someone", "pwd"));
        physicalBookService.addPhysicalBook(physicalBookRequest("Unauthenticated add"));
        assertThat(oneRow().get("username")).isEqualTo("anonymous");
    }

    @Test
    void bookDigitized_cascadeIsAuditedUnderTriggeringUser() throws IOException {
        String reader = loginAs(UserRole.READER);
        UUID readerId = idOf(reader);
        saveLibraryCard(readerId, "bababooey@example.com");
        PhysicalBookResponse book = savePhysicalBook("I WILL BE DIGITIZED");
        saveOpenOrder(readerId, book.id());
        String archive = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        digitizedBookService.addDigitizedBook(digitizedBookRequest("I WILL BE DIGITIZED", book.id()), book.id(),
                TestFiles.sampleMultipartPdf(), null);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Map<String, Object>> rows = auditRows();
            assertThat(rows).anySatisfy(r -> {
                assertSuccess(r, archive, "BookOrderServiceImpl.updateStatus(..)");
            });
            assertThat(rows).anySatisfy(r -> {
                assertSuccess(r, archive, "PhysicalBookServiceImpl.deletePhysicalBook(..)");
            });
        });
    }


    String loginAs(UserRole role) {
        String username = createUser(role);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(username,
                null, List.of()));
        return username;
    }

    UUID idOf(String username) {
        return jdbc.sql("SELECT id FROM users WHERE username = :u").param("u", username).query(UUID.class).single();
    }

    List<Map<String, Object>> auditRows() {
        return jdbc.sql("SELECT * FROM audit_log ORDER BY occurred_at").query().listOfRows();
    }

    Map<String, Object> oneRow() {
        List<Map<String, Object>> rows = auditRows();
        assertThat(rows).hasSize(1);
        return rows.getFirst();
    }

    Map<String, Object> oneRowOf(String method) {
        List<Map<String, Object>> rows = auditRows().stream().filter(r -> method.equals(r.get("method"))).toList();
        assertThat(rows).hasSize(1);
        return rows.getFirst();
    }

    void resetAudit() {
        jdbc.sql("DELETE FROM audit_log").update();
    }

    void assertSuccess(Map<String, Object> row, String username, String method) {
        assertThat(row.get("username")).isEqualTo(username);
        assertThat(row.get("method")).isEqualTo(method);
        assertThat((Boolean) row.get("failed")).isFalse();
        assertThat(row.get("occurred_at")).isNotNull();
        assertThat(((Number) row.get("duration_ms")).longValue()).isGreaterThanOrEqualTo(0);
    }

    void assertFailure(Map<String, Object> row, String username, String method, Class<? extends Throwable> expected) {
        assertThat(row.get("username")).isEqualTo(username);
        assertThat(row.get("method")).isEqualTo(method);
        assertThat((Boolean) row.get("failed")).isTrue();
        assertThat((String) row.get("result")).startsWith(expected.getName());
        assertThat(row.get("occurred_at")).isNotNull();
    }

    void awaitDigitizationCascade(UUID physicalBookId) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(auditRows()).anySatisfy(r -> {
                    assertThat(r.get("method")).isEqualTo("PhysicalBookServiceImpl.deletePhysicalBook(..)");
                    assertThat((String) r.get("args")).contains(physicalBookId.toString());
                }));
    }

    PhysicalBookRequest physicalBookRequest(String name) {
        return new PhysicalBookRequest(name, List.of("Test Author"), "Description", "Історична",
                Year.of(1900), "ukr", "Книга");
    }

    DigitizedBookRequest digitizedBookRequest(String name, UUID physicalBookId) {
        return new DigitizedBookRequest(name, List.of("Dickinson W. Howship"),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false,
                physicalBookId);
    }

    LibraryCardRequest libraryCardRequest(String email) {
        return new LibraryCardRequest(email, "01001", Year.of(2007), "Albuquerque, NM, 467102",
                "Albuquerque, NM, 403742", "Meth", "Cook");
    }

    User newUser(String username, String rawPassword, UserRole role) {
        return new User(username, rawPassword, "Jane", "Doe", "Andriivna", role, true);
    }

    PhysicalBookResponse savePhysicalBook(String name) {
        PhysicalBookResponse response = physicalBookService.addPhysicalBook(physicalBookRequest(name));
        resetAudit();
        return response;
    }

    void saveLibraryCard(UUID userId, String email) {
        libraryCardService.addLibraryCard(userId, libraryCardRequest(email));
        resetAudit();
    }

    DigitizedBookResponse saveDigitizedBook(String name) throws IOException {
        UUID physicalBookId = UUID.randomUUID();
        DigitizedBookResponse response = digitizedBookService.addDigitizedBook(
                digitizedBookRequest(name, physicalBookId), physicalBookId, TestFiles.sampleMultipartPdf(), null);
        awaitDigitizationCascade(physicalBookId);
        resetAudit();
        return response;
    }

    BookOrderResponse saveOpenOrder(UUID userId, UUID physicalBookId) {
        BookOrderResponse response = bookOrderService.addBookOrder(new BookOrderRequest(null,
                physicalBookId, true), userId);
        resetAudit();
        return response;
    }

}
