package ua.fictionallibrary.digital_lib.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ForbiddenPage {
    private final Page page;

    public ForbiddenPage(Page page) {
        this.page = page;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Доступ заборонено"));
    }

    public PhysicalBooksPage goHome() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("На головну")).click();
        return new PhysicalBooksPage(page);
    }
}
