package org.ifmo.ru.lab4back.functional;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("functional")
public class FunctionalTest {
    static Playwright playwright;
    static Browser browser;

    static String sharedLogin = null;
    static String sharedPassword = null;

    BrowserContext context1;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setChannel("chrome")
                .setHeadless(false)); // можно тру, чтобы не видеть как браузер открывается
        System.out.println("Browser launched");
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
            System.out.println("Browser closed");
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context1 = browser.newContext();
        page = context1.newPage();
        page.setViewportSize(1920, 1080);
        page.setDefaultTimeout(30000);
        page.setDefaultNavigationTimeout(30000);
    }

    void logoutIfNeeded() {
        if (page == null) return;

        try {
            Locator logout = page.locator("a:has-text(\"Log out\")");
            if (logout.count() > 0 && logout.isVisible()) {
                System.out.println("Logging out after test...");
                try {
                    logout.click();
                    page.waitForURL("**/start-page", new Page.WaitForURLOptions().setTimeout(7000));
                } catch (PlaywrightException e) {
                    System.err.println("Logout clicked but start-page not detected: " + e.getMessage());
                }
            } else if (page.url() != null && page.url().contains("/main-page")) {
                try {
                    page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
                } catch (PlaywrightException e) {
                    System.err.println("Failed to navigate to start-page during logoutIfNeeded: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Exception in logoutIfNeeded: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDownAndLogout() {
        try {
            logoutIfNeeded();
        } finally {
            if (context1 != null) {
                try {
                    context1.close();
                } catch (Exception e) {
                    System.err.println("Failed to close context: " + e.getMessage());
                } finally {
                    context1 = null;
                    page = null;
                }
            }
        }
    }


    @AfterEach
    void closeContext() {
        if (context1 != null) context1.close();
    }

    @Test
    @Order(1)
    void testRegisterAccount() {
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");

        page.locator("button:has-text(\"Registration Form\")").click();

        try {
            page.waitForSelector("#login", new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (PlaywrightException e) {
            try { page.waitForSelector("#password", new Page.WaitForSelectorOptions().setTimeout(5000)); }
            catch (PlaywrightException ex) {
                Paths.get("screenshots").toFile().mkdirs();
                fail("Registration form did not appear.");
                return;
            }
        }

        sharedLogin = "user_" + UUID.randomUUID().toString().substring(0, 8);
        sharedPassword = "Password123";

        Locator loginLocator = page.locator("#login");
        if (loginLocator.count() == 0) loginLocator = page.locator("input[name='login']").first();
        Locator passwordLocator = page.locator("#password");
        if (passwordLocator.count() == 0) passwordLocator = page.locator("input[type='password']").first();
        Locator repeatedLocator = page.locator("#repeatedPassword");
        if (repeatedLocator.count() == 0) repeatedLocator = page.locator("input[type='password']").nth(1);

        loginLocator.fill(sharedLogin);
        page.keyboard().press("Tab");
        page.waitForTimeout(150);
        passwordLocator.fill(sharedPassword);
        repeatedLocator.fill(sharedPassword);

        page.locator("button:has-text(\"Enter\")").click();
        try {
            page.waitForURL("**/main-page", new Page.WaitForURLOptions().setTimeout(10000));
        } catch (PlaywrightException e) {
            Paths.get("screenshots").toFile().mkdirs();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/registration-no-nav.png")));
            System.out.println("PAGE CONTENT AFTER SUBMIT:\n" + page.content());
            fail("Registration did not lead to main-page.");
            return;
        }

        assertEquals("http://localhost:8080/demo-1.0-SNAPSHOT/main-page", page.url());
    }

    void loginAs(String login, String password) {
        if (login == null || password == null) {
            fail("Login credentials are null — run registration test first.");
            return;
        }

        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");

        Locator startLogin = page.locator("#login");
        if (startLogin.count() == 0) startLogin = page.locator("input[name='login']").first();
        startLogin.fill(login);

        page.locator("button:has-text(\"Check Login\")").click();

        try { page.waitForSelector("#password", new Page.WaitForSelectorOptions().setTimeout(5000)); }
        catch (PlaywrightException ignore) {}

        Locator loginPassword = page.locator("#password");
        if (loginPassword.count() == 0) loginPassword = page.locator("input[type='password']").first();
        loginPassword.fill(password);

        page.locator("button:has-text(\"Enter\")").click();
        page.waitForURL("**/main-page", new Page.WaitForURLOptions().setTimeout(10000));
    }

    @Test
    @Order(2)
    void testLoginWithRegisteredAccount() {
        loginAs(sharedLogin, sharedPassword);
        assertEquals("http://localhost:8080/demo-1.0-SNAPSHOT/main-page", page.url());

        try { page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/login-success.png"))); } catch (Exception ignored) {}
    }

    int countCirclesWithFill(String fill) {
        Locator circles = page.locator("svg#graph circle[fill='" + fill + "']");
        return circles.count();
    }

    @Test
    @Order(3)
    void testClickRectAddsGreenDot() {
        loginAs(sharedLogin, sharedPassword);
        page.waitForSelector("svg#graph", new Page.WaitForSelectorOptions().setTimeout(5000));

        Locator rect = page.locator("svg#graph rect");
        assertTrue(rect.count() > 0, "rect on graph not found");
        rect.first().click();

        page.waitForSelector("svg#graph circle[fill='green']", new Page.WaitForSelectorOptions().setTimeout(5000));
        assertTrue(countCirclesWithFill("green") > 0, "Green circle was not added after clicking rect");
    }

    @Test
    @Order(4)
    void testFormSubmitAddsRedDot() {
        loginAs(sharedLogin, sharedPassword);
        page.waitForSelector("svg#graph", new Page.WaitForSelectorOptions().setTimeout(5000));

        int before = countCirclesWithFill("red");

        Locator xRadio = page.locator("input[name='xValue'][type='radio']").first();
        xRadio.click();

        Locator yInput = page.locator("input#y");
        if (yInput.count() == 0) yInput = page.locator("input[name='y']");
        yInput.fill("0");
        page.keyboard().press("Tab");

        Locator rRadio = page.locator("input[name='rValue'][type='radio']").first();
        rRadio.click();

        page.locator("button:has-text(\"Submit\")").click();

        page.waitForTimeout(500);
        int after = countCirclesWithFill("red");
        assertTrue(after > before, "Red circle was not added after form submit");
    }

    @Test
    @Order(5)
    void testCheckUnregisteredLoginShowsError() {
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
        String fake = "unreg_" + UUID.randomUUID().toString().substring(0, 8);
        page.locator("input#login, input[name='login']").first().fill(fake);
        page.locator("button:has-text(\"Check Login\")").click();

        page.waitForSelector("text=Login is incorrect. Please try again.", new Page.WaitForSelectorOptions().setTimeout(5000));
        assertTrue(page.locator("text=Login is incorrect. Please try again.").count() > 0);
    }

    @Test
    @Order(6)
    void testRegisteredLoginWrongPasswordShowsError() {
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
        page.locator("input#login, input[name='login']").first().fill(sharedLogin);
        page.locator("button:has-text(\"Check Login\")").click();
        try { page.waitForSelector("#password", new Page.WaitForSelectorOptions().setTimeout(3000)); } catch (PlaywrightException ignored) {}
        page.locator("input[type='password']").first().fill("wrong_password");
        page.locator("button:has-text(\"Enter\")").click();

        page.waitForSelector("text=Incorrect password", new Page.WaitForSelectorOptions().setTimeout(5000));
        assertTrue(page.locator("text=Incorrect password").count() > 0);
    }

    @Test
    @Order(7)
    void testRegistrationExistingLoginShowsErrorOnBlur() {
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
        page.locator("button:has-text(\"Registration Form\")").click();
        page.waitForSelector("#login", new Page.WaitForSelectorOptions().setTimeout(5000));

        Locator regLogin = page.locator("#login");
        if (regLogin.count() == 0) regLogin = page.locator("input[name='login']").first();
        regLogin.fill(sharedLogin);
        page.keyboard().press("Tab");

        page.waitForSelector("text=Login is incorrect. Please try again.", new Page.WaitForSelectorOptions().setTimeout(5000));
        assertTrue(page.locator("text=Login is incorrect. Please try again.").count() > 0);
    }

    @Test
    @Order(8)
    void testRegistrationPasswordsMismatchShowsError() {
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
        page.locator("button:has-text(\"Registration Form\")").click();
        page.waitForSelector("#login", new Page.WaitForSelectorOptions().setTimeout(5000));

        String newLogin = "user_" + UUID.randomUUID().toString().substring(0, 8);
        Locator regLogin = page.locator("#login");
        if (regLogin.count() == 0) regLogin = page.locator("input[name='login']").first();
        regLogin.fill(newLogin);
        page.keyboard().press("Tab");

        Locator pwd = page.locator("#password");
        if (pwd.count() == 0) pwd = page.locator("input[type='password']").first();
        Locator rep = page.locator("#repeatedPassword");
        if (rep.count() == 0) rep = page.locator("input[type='password']").nth(1);

        pwd.fill("password1");
        rep.fill("different");
        page.keyboard().press("Tab");

        page.waitForSelector("text=Passwords are not same. Please try again.", new Page.WaitForSelectorOptions().setTimeout(5000));
        assertTrue(page.locator("text=Passwords are not same. Please try again.").count() > 0);
    }

    @Test
    @Order(9)
    void testNegativeYInputClears() {
        loginAs(sharedLogin, sharedPassword);
        Locator yInput = page.locator("input#y");
        if (yInput.count() == 0) yInput = page.locator("input[name='y']");
        yInput.fill("-10");
        page.keyboard().press("Tab");
        String val = yInput.inputValue();
        assertTrue(val == null || val.isEmpty(), "Y input was not cleared after entering -10, current value: '" + val + "'");
    }

    @Test
    @Order(10)
    void testClickGraphAddsRedDotWhenNotOnBlueShapes() {
        loginAs(sharedLogin, sharedPassword);
        page.waitForSelector("svg#graph", new Page.WaitForSelectorOptions().setTimeout(5000));

        int before = countCirclesWithFill("red");

        Locator svg = page.locator("svg#graph");
        try {
            svg.click(new Locator.ClickOptions().setPosition(220, 220));
        } catch (PlaywrightException e) {
            svg.click();
        }

        page.waitForTimeout(500);
        int after = countCirclesWithFill("red");
        assertTrue(after > before, "Red circle was not added after clicking on empty area of svg");
    }

    @Test
    @Order(11)
    void testSelectXShowsNotAllParamsMessage() {
        loginAs(sharedLogin, sharedPassword);
        Locator xRadio = page.locator("input[name='xValue'][type='radio']").first();
        xRadio.click();
        page.waitForSelector("text=Not all parameters selected", new Page.WaitForSelectorOptions().setTimeout(3000));
        assertTrue(page.locator("text=Not all parameters selected").count() > 0);
    }

    @Test
    @Order(12)
    void testLoggedInStartPageRedirectsToMain() {
        loginAs(sharedLogin, sharedPassword);
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
        page.waitForURL("**/main-page", new Page.WaitForURLOptions().setTimeout(5000));
        assertTrue(page.url().contains("/main-page"));
    }

    @Test
    @Order(13)
    void testNotLoggedInMainPageRedirectsToStart() {
        try { logoutIfNeeded(); } catch (Exception ignored) {}
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/start-page");
        page.navigate("http://localhost:8080/demo-1.0-SNAPSHOT/main-page");
        page.waitForURL("**/start-page", new Page.WaitForURLOptions().setTimeout(5000));
        assertTrue(page.url().contains("/start-page"));
    }


    @Test
    @Order(14)
    void testClickInsideFlippedAreaAddsGreenDot() {
        loginAs(sharedLogin, sharedPassword);

        Locator rNegative = page.locator("input[name='rValue'][type='radio'][value^='-']").first();
        if (rNegative.count() == 0) rNegative = page.locator("input[name='rValue'][type='radio']").last();
        rNegative.click();

        page.waitForSelector("svg#graph", new Page.WaitForSelectorOptions().setTimeout(5000));
        int before = countCirclesWithFill("green");

        Locator svg = page.locator("svg#graph");
        try {
            svg.click(new Locator.ClickOptions().setPosition(120, 80));
        } catch (PlaywrightException e) {
            svg.click();
        }

        page.waitForSelector("svg#graph circle[fill='green']", new Page.WaitForSelectorOptions().setTimeout(5000));
        int after = countCirclesWithFill("green");
        assertTrue(after > before, "Green circle was not added after clicking inside flipped area");
    }

    @Test
    @Order(15)
    void testClickOutsideFlippedAreaAddsRedDot() {
        loginAs(sharedLogin, sharedPassword);

        Locator rNegative = page.locator("input[name='rValue'][type='radio'][value^='-']").first();
        if (rNegative.count() == 0) rNegative = page.locator("input[name='rValue'][type='radio']").last();
        rNegative.click();

        page.waitForSelector("svg#graph", new Page.WaitForSelectorOptions().setTimeout(5000));
        int before = countCirclesWithFill("red");

        Locator svg = page.locator("svg#graph");
        try {
            svg.click(new Locator.ClickOptions().setPosition(260, 260));
        } catch (PlaywrightException e) {
            svg.click();
        }

        page.waitForSelector("svg#graph circle[fill='red']", new Page.WaitForSelectorOptions().setTimeout(5000));
        int after = countCirclesWithFill("red");
        assertTrue(after > before, "Red circle was not added after clicking outside flipped area");
    }

}
