package ua.fictionallibrary.digital_lib.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class UsersPage {
    public static final String PATH = "/users";

    private final Page page;

    public UsersPage(Page page) {
        this.page = page;
    }

    public UsersPage open() {
        page.navigate(PATH);
        return this;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Користувачі"));
    }

    public UserFormPage clickNewUser() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("+ Новий користувач")).click();
        return new UserFormPage(page);
    }

    public Locator successMessage() {
        return page.getByRole(AriaRole.STATUS);
    }

    public Locator errorMessage() {
        return page.getByRole(AriaRole.ALERT);
    }

    public Locator searchInput() {
        return page.getByLabel("Пошук за ПІБ або логіном");
    }

    public Locator roleFilter() {
        return page.getByLabel("Роль", new Page.GetByLabelOptions().setExact(true));
    }

    public Locator rows() {
        return page.locator("table tbody tr:not(.table__empty)");
    }

    public UserRow user(String username) {
        return new UserRow(rows().filter(new Locator.FilterOptions()
                .setHas(page.locator("td", new Page.LocatorOptions().setHasText(username)))));
    }

    public static class UserRow {
        private final Locator row;

        UserRow(Locator row) {
            this.row = row;
        }

        public Locator self() { return row; }

        public Locator fullName() { return row.locator("td").nth(0); }

        public Locator role() { return row.locator("select"); }

        public Locator status() { return row.locator(".badge"); }

        public Locator toggleActive() { return row.getByRole(AriaRole.BUTTON); }
    }
}
