package ua.fictionallibrary.digital_lib.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import ua.fictionallibrary.digital_lib.e2e.pages.ForbiddenPage;
import ua.fictionallibrary.digital_lib.e2e.pages.LibraryCardsPage;
import ua.fictionallibrary.digital_lib.e2e.pages.LoginPage;
import ua.fictionallibrary.digital_lib.e2e.pages.UserFormPage;
import ua.fictionallibrary.digital_lib.e2e.pages.UsersPage;
import ua.fictionallibrary.digital_lib.support.E2ETest;
import ua.fictionallibrary.digital_lib.user.LibraryCardService;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;

import java.time.Year;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
class AdminUITest extends BrowserTestSupport {
    private static final String ADMIN = "queen";
    private static final String ADMIN_PASSWORD = "M@m@@@@@@@!!!!___";
    private static final String READER = "username666";
    private static final String READER_PASSWORD = "abba123";

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private LibraryCardService libraryCardService;

    @Test
    void adminCreatesEmployeeChangesRoleAndDeactivates_employeeCanNoLongerLogIn() {
        loginAs(ADMIN, ADMIN_PASSWORD);
        String username = uniqueUsername();

        UsersPage users = new UsersPage(page).open().clickNewUser()
                .fill(username, "Secret123!", "Коваленко", "Олена", "Відділ доставки книг")
                .save();

        assertThat(page).hasURL(Pattern.compile(UsersPage.PATH + "$"));
        assertThat(users.successMessage()).hasText("Користувача створено");
        UsersPage.UserRow row = users.user(username);
        assertThat(row.fullName()).hasText("Коваленко Олена");
        assertThat(row.role()).hasValue("BOOK_DELIVERY_DEPARTMENT");
        assertThat(row.status()).hasText("Активний");

        row.role().selectOption("Відділ цифрового архіву");
        assertThat(users.successMessage()).hasText("Роль змінено");
        assertThat(new UsersPage(page).open().user(username).role()).hasValue("DIGITAL_ARCHIVE_DEPARTMENT");

        row.toggleActive().click();
        assertThat(users.successMessage()).hasText("Обліковий запис деактивовано");
        assertThat(row.status()).hasText("Деактивований");
        assertThat(row.toggleActive()).hasText("Активувати");

        logout();
        new LoginPage(page).login(username, "Secret123!");
        assertThat(page.getByRole(AriaRole.ALERT)).hasText("Невірний логін або пароль");
        assertThat(page).hasURL(Pattern.compile("/login$"));
    }

    @Test
    void createdEmployeeCanLogIn() {
        loginAs(ADMIN, ADMIN_PASSWORD);
        String username = uniqueUsername();
        new UserFormPage(page).open().fill(username, "Secret123!", "Бондар", "Іван", "Відділ цифрового архіву").save();
        assertThat(new UsersPage(page).successMessage()).hasText("Користувача створено");

        logout();
        loginAs(username, "Secret123!");
    }

    @Test
    void reactivatedUserCanLogInAgain() {
        loginAs(ADMIN, ADMIN_PASSWORD);
        String username = uniqueUsername();
        UsersPage users = new UserFormPage(page).open()
                .fill(username, "Secret123!", "Мельник", "Петро", "Читач").save();

        UsersPage.UserRow row = users.user(username);
        row.toggleActive().click();
        assertThat(row.status()).hasText("Деактивований");
        row.toggleActive().click();
        assertThat(users.successMessage()).hasText("Обліковий запис активовано");
        assertThat(row.status()).hasText("Активний");

        logout();
        new LoginPage(page).login(username, "Secret123!");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Оцифровані книги"))).isVisible();
    }

    @Test
    void adminCannotChangeOwnRoleOrDeactivateSelf() {
        loginAs(ADMIN, ADMIN_PASSWORD);

        UsersPage.UserRow self = new UsersPage(page).open().user(ADMIN);

        assertThat(self.role()).isDisabled();
        assertThat(self.toggleActive()).isDisabled();
    }

    @Test
    void duplicateUsernameShowsError() {
        loginAs(ADMIN, ADMIN_PASSWORD);

        UserFormPage form = new UserFormPage(page).open().fill(READER, "Secret123!", "Дубль", "Дубль", "Читач");
        form.saveExpectingErrors();

        assertThat(form.errorMessage()).hasText("Користувач із таким логіном уже існує");
        assertThat(page).hasURL(Pattern.compile(UserFormPage.PATH + "$"));
    }

    @Test
    void emptyFormIsNotSubmitted() {
        loginAs(ADMIN, ADMIN_PASSWORD);

        UserFormPage form = new UserFormPage(page).open();
        form.saveExpectingErrors();

        assertThat(page).hasURL(Pattern.compile(UserFormPage.PATH + "$"));
        assertThat(form.usernameInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.passwordInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.surnameInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.nameInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.patronymicInput()).not().hasClass(Pattern.compile("input--invalid"));
    }

    @Test
    void usersCanBeFilteredByRoleAndSearch() {
        loginAs(ADMIN, ADMIN_PASSWORD);
        UsersPage users = new UsersPage(page).open();

        users.roleFilter().selectOption("Адміністратор");
        assertThat(users.user(ADMIN).self()).hasCount(1);
        assertThat(users.user(READER).self()).hasCount(0);

        users.roleFilter().selectOption("Роль: усі");
        users.searchInput().fill(READER);
        assertThat(users.rows()).hasCount(1);
        assertThat(users.user(READER).self()).hasCount(1);
    }

    @Test
    void adminSeesLibraryCardsWithOwnerName() {
        UUID readerId = jdbc.sql("SELECT id FROM users WHERE username = :u").param("u", READER)
                .query(UUID.class).single();
        jdbc.sql("DELETE FROM library_cards WHERE owner_id = :id").param("id", readerId).update();
        String email = "reader-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        libraryCardService.addLibraryCard(readerId,
                new LibraryCardRequest(email, "01001", Year.of(1990), "Київ, вул. Хрещатик, 1", null, null, null));
        String[] name = jdbc.sql("SELECT surname, name FROM users WHERE id = :id").param("id", readerId)
                .query((rs, i) -> new String[]{rs.getString(1), rs.getString(2)}).single();

        loginAs(ADMIN, ADMIN_PASSWORD);
        LibraryCardsPage cards = new LibraryCardsPage(page).open();

        assertThat(cards.heading()).isVisible();
        var row = cards.rowByEmail(email);
        assertThat(row).hasCount(1);
        assertThat(row).containsText(name[0] + " " + name[1]);
        assertThat(row).containsText("1990");
        assertThat(row).containsText("Київ, вул. Хрещатик, 1");
    }

    @Test
    void readerIsForbiddenToOpenAdminPages() {
        loginAs(READER, READER_PASSWORD);

        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Користувачі"))).hasCount(0);
        for (String path : new String[]{UsersPage.PATH, UserFormPage.PATH, LibraryCardsPage.PATH}) {
            page.navigate(path);
            assertThat(new ForbiddenPage(page).heading()).isVisible();
        }
    }

    private void logout() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Вийти")).click();
        assertThat(new LoginPage(page).heading()).isVisible();
    }

    private static String uniqueUsername() {
        return "employee_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
