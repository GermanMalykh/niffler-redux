package guru.qa.niffler.test;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;

public class BaseUiTest {
    @BeforeAll
    public static void setupSelenoid() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true));
        Configuration.browserSize = "1920x1080";
        Configuration.headless = false;
        Configuration.pageLoadStrategy = "eager";
        Configuration.baseUrl = "http://127.0.0.1:3000";
    }
}
