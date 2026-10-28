package ua.fictionallibrary.digital_lib.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

abstract class BrowserTestSupport {

    private static final Path ARTIFACTS = Paths.get("target", "playwright");

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    protected BrowserContext context;
    protected Page page;
    private boolean tracingSaved;

    @RegisterExtension
    final AfterTestExecutionCallback saveArtifactsOnFailure = extension -> {
        String name = extension.getRequiredTestClass().getSimpleName() + "-" + extension.getDisplayName()
                .replaceAll("[^\\p{L}\\p{N}_-]", "");
        page.screenshot(new Page.ScreenshotOptions().setPath(ARTIFACTS.resolve(name + ".png")).setFullPage(true));
        context.tracing().stop(new Tracing.StopOptions().setPath(ARTIFACTS.resolve(name + "-trace.zip")));
        tracingSaved = true;

    };

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL("http://localhost:" + port));
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
        tracingSaved = false;
    }

    @AfterEach
    void closePage() {
        if (!tracingSaved) {
            context.tracing().stop();
        }
        context.close();
    }

    protected void loginAs(String username, String password) {
        page.navigate("/login");
        page.getByLabel("Логін").fill(username);
        page.getByLabel("Пароль").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Увійти")).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Фізичні книги")))
                .isVisible();
    }
}
