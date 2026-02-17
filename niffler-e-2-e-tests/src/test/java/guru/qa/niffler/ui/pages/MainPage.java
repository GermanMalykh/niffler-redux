package guru.qa.niffler.ui.pages;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.ui.elements.SpendingsTable;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class MainPage extends BasePage<MainPage> {
    private final SpendingsTable spendingsTable = new SpendingsTable();
    private final SelenideElement LOGIN_BUTTON = $("a[href*='redirect']");
    private final SelenideElement DELETE_SELECTED_CATEGORY_BUTTON = $(byText("Delete selected"));
    private final SelenideElement SECTION_STATS = $(".main-content__section-stats");

    @Override
    public String url() {
        return "/main";
    }

    @Step("Click login button")
    public MainPage clickLoginButton() {
        LOGIN_BUTTON.click();
        return this;
    }

    @Step("Delete spending with description {0}")
    public MainPage deleteSpending(String description) {
        spendingsTable.selectRowByDescription(description);
        DELETE_SELECTED_CATEGORY_BUTTON.click();
        return this;
    }

    @Step("Check that spendings table is empty")
    public MainPage shouldHaveNoSpendings() {
        spendingsTable.shouldBeEmpty();
        return this;
    }

    @Step("Check that section stats is displayed")
    public MainPage shouldHaveSectionStats() {
        SECTION_STATS.should(visible);
        return this;
    }
}
