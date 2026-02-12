package guru.qa.niffler.ui.tests;

import guru.qa.niffler.api.model.CurrencyValues;
import guru.qa.niffler.api.model.SpendJson;
import guru.qa.niffler.common.jupiter.annotation.GenerateCategory;
import guru.qa.niffler.common.jupiter.annotation.GenerateSpend;
import guru.qa.niffler.ui.pages.AuthPage;
import guru.qa.niffler.ui.pages.MainPage;
import org.junit.jupiter.api.Test;

public class SpendingTest extends BaseUiTest {
    MainPage main = new MainPage();

    @GenerateCategory(
            category = "Обучение",
            username = "Pizzly"
    )
    @GenerateSpend(
            username = "Pizzly",
            description = "QA.GURU Advanced 4",
            amount = 72500.00,
            category = "Обучение",
            currency = CurrencyValues.RUB
    )
    @Test
    void spendingShouldBeDeletedByButtonDeleteSpending(SpendJson spend) {
        main.open().clickLoginButton()
                .getPage(AuthPage.class)
                .loginAs("Pizzly", "12345")
                .getPage(MainPage.class)
                .deleteSpending(spend.description())
                .shouldHaveNoSpendings();
    }
}
