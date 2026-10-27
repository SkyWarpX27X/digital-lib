package ua.fictionallibrary.digital_lib.e2e;

import org.junit.jupiter.api.Test;
import ua.fictionallibrary.digital_lib.e2e.pages.AddPhysicalBookPage;
import ua.fictionallibrary.digital_lib.e2e.pages.ForbiddenPage;
import ua.fictionallibrary.digital_lib.e2e.pages.LoginPage;
import ua.fictionallibrary.digital_lib.e2e.pages.PhysicalBooksPage;
import ua.fictionallibrary.digital_lib.support.E2ETest;

import java.util.UUID;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
class UITest extends BrowserTestSupport {
    @Test
    void unauthenticatedUserIsRedirectedToLogin() {
        new PhysicalBooksPage(page).open();

        assertThat(page).hasURL(Pattern.compile("/login$"));
        assertThat(new LoginPage(page).heading()).isVisible();
    }

    @Test
    void userIsReturnedToRequestedPageAfterLogin() {
        AddPhysicalBookPage form = new AddPhysicalBookPage(page).open();
        assertThat(page).hasURL(Pattern.compile("/login$"));

        new LoginPage(page).login("rogt", "1m_1nl0veW!thMy__C@r");

        assertThat(page).hasURL(Pattern.compile(AddPhysicalBookPage.PATH + "(\\?continue)?$"));
        assertThat(form.heading()).isVisible();
    }

    @Test
    void adminShouldAddBook() {
        loginAs("queen", "M@m@@@@@@@!!!!___");
        String name = uniqueName();

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();
        fillBook(form, name, "Іван Франко", "1990", "Книга");
        PhysicalBooksPage list = form.save();

        assertThat(page).hasURL(Pattern.compile("/physical-books$"));
        assertThat(list.successMessage()).hasText("Книгу успішно додано");
        assertThat(list.rowByName(name)).hasCount(1);
    }

    @Test
    void addBookWithInvalidDataAndGetError() {
        loginAs("rogt", "1m_1nl0veW!thMy__C@r");

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();
        form.nameInput().fill(uniqueName());
        form.publishingYearInput().fill("2999");
        form.saveExpectingErrors();

        assertThat(page).hasURL(Pattern.compile("/physical-books$"));
        assertThat(form.heading()).isVisible();
        assertThat(form.nameInput()).not().hasClass(Pattern.compile("input--invalid"));
        assertThat(form.authorsInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.descriptionInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.topicInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.publishingYearInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.languageInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(form.resourceTypeInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(page.getByText("Рік публікації не може бути у майбутньому")).isVisible();
    }

    @Test
    void otherUsersAreForbiddenToOpenAddBookForm() {
        loginAs("username666", "abba123");

        assertThat(new PhysicalBooksPage(page).addBookButton()).hasCount(0);

        new AddPhysicalBookPage(page).open();
        ForbiddenPage forbidden = new ForbiddenPage(page);
        assertThat(forbidden.heading()).isVisible();
        assertThat(forbidden.goHome().heading()).isVisible();
    }

    @Test
    void addBookButtonOpensForm() {
        loginAs("rogt", "1m_1nl0veW!thMy__C@r");

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();

        assertThat(page).hasURL(Pattern.compile(AddPhysicalBookPage.PATH + "$"));
        assertThat(form.heading()).isVisible();
    }

    @Test
    void addedBookAppearsInList() {
        loginAs("rogt", "1m_1nl0veW!thMy__C@r");
        String name = uniqueName();

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();
        fillBook(form, name, "Іван Франко", "1700", "Стародрук");
        PhysicalBooksPage.BookRow row = form.save().book(name);

        assertThat(row.name()).hasText(name);
        assertThat(row.authors()).hasText("Іван Франко");
        assertThat(row.topic()).hasText("Художня література");
        assertThat(row.publishingYear()).hasText("1700");
        assertThat(row.language()).hasText("Українська");
        assertThat(row.resourceType()).hasText("Стародрук");
    }

    @Test
    void multipleAuthorsAreSplitByComma() {
        loginAs("rogt", "1m_1nl0veW!thMy__C@r");
        String name = uniqueName();

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();
        fillBook(form, name, " Леся Українка ,, Тарас Шевченко ", "1990", "Книга");
        PhysicalBooksPage.BookRow row = form.save().book(name);

        assertThat(row.authors()).containsText("Леся Українка");
        assertThat(row.authors()).containsText("Тарас Шевченко");
        assertThat(row.authors()).hasText(Pattern.compile("^[^,]+, [^,]+$"));
    }

    @Test
    void rejectedBookIsNotAddedToList() {
        loginAs("rogt", "1m_1nl0veW!thMy__C@r");
        String name = uniqueName();

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();
        fillBook(form, name, "Іван Франко", "2000", "Стародрук");
        form.saveExpectingErrors();

        assertThat(form.resourceTypeInput()).hasClass(Pattern.compile("input--invalid"));
        assertThat(new PhysicalBooksPage(page).open().rowByName(name)).hasCount(0);
    }

    @Test
    void cancelReturnsToListWithoutSaving() {
        loginAs("rogt", "1m_1nl0veW!thMy__C@r");
        String name = uniqueName();

        AddPhysicalBookPage form = new PhysicalBooksPage(page).clickAddBook();
        fillBook(form, name, "Іван Франко", "1990", "Книга");
        PhysicalBooksPage list = form.cancel();

        assertThat(page).hasURL(Pattern.compile("/physical-books$"));
        assertThat(list.successMessage()).hasCount(0);
        assertThat(list.rowByName(name)).hasCount(0);
    }

    private static String uniqueName() {
        return "Тестова книга " + UUID.randomUUID();
    }

    private static void fillBook(AddPhysicalBookPage form, String name, String authors, String year, String resourceType) {
        form.nameInput().fill(name);
        form.authorsInput().fill(authors);
        form.descriptionInput().fill("Опис тестової книги");
        form.topicInput().fill("Художня література");
        form.publishingYearInput().fill(year);
        form.languageInput().fill("Українська");
        form.resourceTypeInput().fill(resourceType);
    }
}
