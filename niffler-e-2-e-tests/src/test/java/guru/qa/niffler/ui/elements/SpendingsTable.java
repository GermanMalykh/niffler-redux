package guru.qa.niffler.ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.ScrollIntoViewOptions.Block.center;
import static com.codeborne.selenide.ScrollIntoViewOptions.instant;
import static com.codeborne.selenide.Selenide.$;

public class SpendingsTable {
    private final SelenideElement TABLE = $("table.spendings-table");

    private ElementsCollection rows() {
        return TABLE.$("tbody").$$("tr");
    }

    @Step("Get row by description {0}")
    public void selectRowByDescription(String description) {
        rows()
                .find(text(description))
                .$$("td")
                .first()
                .scrollIntoView(instant().block(center))
                .click();
    }

    @Step("Check that spendings table is empty")
    public void shouldBeEmpty() {
        rows().shouldHave(size(0));
    }
}


