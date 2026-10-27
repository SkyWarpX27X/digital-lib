package ua.fictionallibrary.digital_lib.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class AddPhysicalBookPage {
    public static final String PATH = "/physical-books/new";

    private final Page page;

    public AddPhysicalBookPage(Page page) {
        this.page = page;
    }

    public AddPhysicalBookPage open() {
        page.navigate(PATH);
        return this;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Додати фізичну книгу"));
    }

    public PhysicalBooksPage save() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Зберегти")).click();
        return new PhysicalBooksPage(page);
    }

    public void saveExpectingErrors() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Зберегти")).click();
    }

    public PhysicalBooksPage cancel() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Скасувати")).click();
        return new PhysicalBooksPage(page);
    }

    public Locator nameInput() { return page.locator("#name"); }

    public Locator authorsInput() { return page.locator("#authors"); }

    public Locator descriptionInput() { return page.locator("#description"); }

    public Locator topicInput() { return page.locator("#topic"); }

    public Locator publishingYearInput() { return page.locator("#publishingYear"); }

    public Locator languageInput() { return page.locator("#language"); }

    public Locator resourceTypeInput() { return page.locator("#resourceType"); }
}
