package guru.qa.niffler.ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class AuthPage extends BasePage<AuthPage> {
    private final SelenideElement USER_NAME_FIELD = $("input[name='username']");
    private final SelenideElement PASSWORD_FIELD = $("input[name='password']");
    private final SelenideElement LOGIN_BUTTON = $("button[type='submit']");

    @Override
    public String url() {
        return "/login";
    }

    @Step("Fill username field")
    public AuthPage fillUsername(String username) {
        USER_NAME_FIELD.setValue(username);
        return this;
    }

    @Step("Fill password field")
    public AuthPage fillPassword(String password) {
        PASSWORD_FIELD.setValue(password);
        return this;
    }

    @Step("Click login button")
    public AuthPage clickLoginButton() {
        LOGIN_BUTTON.click();
        return this;
    }

    @Step("Login as {0}")
    public AuthPage loginAs(String username, String password) {
        return fillUsername(username).
                fillPassword(password).
                clickLoginButton();
    }
}
