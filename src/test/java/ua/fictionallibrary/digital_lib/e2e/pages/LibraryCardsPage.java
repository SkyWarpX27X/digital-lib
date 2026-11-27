package ua.fictionallibrary.digital_lib.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LibraryCardsPage {
    public static final String PATH = "/library-cards";

    private final Page page;

    public LibraryCardsPage(Page page) {
        this.page = page;
    }

    public LibraryCardsPage open() {
        page.navigate(PATH);
        return this;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Картки читачів"));
    }

    public Locator rowByEmail(String email) {
        return page.locator("table tbody tr").filter(new Locator.FilterOptions()
                .setHas(page.locator("td", new Page.LocatorOptions().setHasText(email))));
    }
}
