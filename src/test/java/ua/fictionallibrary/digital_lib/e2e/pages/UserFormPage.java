package ua.fictionallibrary.digital_lib.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class UserFormPage {
    public static final String PATH = "/users/new";

    private final Page page;

    public UserFormPage(Page page) {
        this.page = page;
    }

    public UserFormPage open() {
        page.navigate(PATH);
        return this;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Новий користувач"));
    }

    public Locator usernameInput() { return page.getByLabel("Логін"); }

    public Locator passwordInput() { return page.getByLabel("Пароль"); }

    public Locator surnameInput() { return page.getByLabel("Прізвище"); }

    public Locator nameInput() { return page.getByLabel("Ім'я"); }

    public Locator patronymicInput() { return page.getByLabel("По батькові"); }

    // exact: "Роль" is a substring of "Пароль"
    public Locator roleSelect() { return page.getByLabel("Роль", new Page.GetByLabelOptions().setExact(true)); }

    public Locator errorMessage() { return page.getByRole(AriaRole.ALERT); }

    public UserFormPage fill(String username, String password, String surname, String name, String roleLabel) {
        usernameInput().fill(username);
        passwordInput().fill(password);
        surnameInput().fill(surname);
        nameInput().fill(name);
        roleSelect().selectOption(roleLabel);
        return this;
    }

    public UsersPage save() {
        submit();
        return new UsersPage(page);
    }

    public void saveExpectingErrors() {
        submit();
    }

    private void submit() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Створити")).click();
    }
}
