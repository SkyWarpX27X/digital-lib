package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.user.LibraryCardService;
import ua.fictionallibrary.digital_lib.user.UserRole;
import ua.fictionallibrary.digital_lib.user.UserService;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
        jdbc.sql("DELETE FROM audit_log").update();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
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

    @Test
    void addDigitizedBook_auditsSuccessfully() throws IOException {
        String username = loginAs(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
        UUID physicalBookId = UUID.randomUUID();
        DigitizedBookRequest book = new DigitizedBookRequest("King Arthur in Cornwall", List.of("Dickinson W. Howship"),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false,
                physicalBookId);
        DigitizedBookResponse response = digitizedBookService.addDigitizedBook(book, physicalBookId,
                TestFiles.sampleMultipartPdf(), null);
        assertThat(jdbc.sql("SELECT count(*) FROM digitized_books").query(Long.class).single()).isEqualTo(1);
        Map<String, Object> row = oneRow();
        assertThat(row.get("username")).isEqualTo(username);
        assertThat(row.get("method")).isEqualTo("DigitizedBookServiceImpl.addDigitizedBook(..)");
        assertTrue(((String) row.get("args")).contains("King Arthur in Cornwall"));
        assertThat(row.get("result")).isEqualTo(response.toString());
        assertFalse((boolean) row.get("failed"));
        assertNotNull(row.get("occurred_at"));
    }

}
