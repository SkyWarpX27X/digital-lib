package ua.fictionallibrary.digital_lib.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class PhysicalBooksPage {
    private final Page page;

    public PhysicalBooksPage(Page page) {
        this.page = page;
    }

    public PhysicalBooksPage open() {
        page.navigate("/physical-books");
        return this;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Фізичні книги"));
    }

    public Locator addBookButton() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Додати фізичну книгу"));
    }

    public AddPhysicalBookPage clickAddBook() {
        addBookButton().click();
        return new AddPhysicalBookPage(page);
    }

    public Locator successMessage() {
        return page.getByRole(AriaRole.STATUS);
    }

    public Locator rowByName(String name) {
        return page.locator("table tbody tr").filter(new Locator.FilterOptions()
                .setHas(page.locator("td.table__primary", new Page.LocatorOptions().setHasText(name))));
    }

    public BookRow book(String name) {
        return new BookRow(rowByName(name));
    }

    public static class BookRow {
        private final Locator row;

        BookRow(Locator row) {
            this.row = row;
        }

        public Locator name() { return cell(0); }

        public Locator authors() { return cell(1); }

        public Locator topic() { return cell(2); }

        public Locator publishingYear() { return cell(3); }

        public Locator language() { return cell(4); }

        public Locator resourceType() { return cell(5); }

        private Locator cell(int index) {
            return row.locator("td").nth(index);
        }
    }
}
