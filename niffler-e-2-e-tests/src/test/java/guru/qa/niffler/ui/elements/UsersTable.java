package guru.qa.niffler.ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class UsersTable {
    private final SelenideElement TABLE = $("table.abstract-table");
    private final String FRIEND_CONFIRMATION_TEXT = "You are friends";
    private final String FRIENDS_NOT_YET_TEXT = "There are no friends yet!";

    @Step("Friendship confirmed")
    public UsersTable verifyFriendshipConfirmed() {
        rows()
                .find(text(FRIEND_CONFIRMATION_TEXT))
                .should(visible);
        return this;
    }

    @Step("Invitation sent")
    public UsersTable verifyIncomingInvitation() {
        TABLE.$("[data-tooltip-id='submit-invitation']")
                .should(visible);
        return this;
    }

    @Step("Check pending invitation")
    public UsersTable verifyPendingInvitation() {
        rows().find(text("Pending invitation"))
                .should(visible);
        return this;
    }

    @Step("Check that friendship is confirmed")
    public boolean hasConfirmedFriendship() {
        waitForTable();
        return rows().stream().anyMatch(row -> row.getText().contains(FRIEND_CONFIRMATION_TEXT));
    }

    @Step("Check that incoming invitation is displayed")
    public boolean hasIncomingInvitation() {
        waitForTable();
        TABLE.$("[data-tooltip-id='submit-invitation']").should(visible);
        return TABLE.$("[data-tooltip-id='submit-invitation']").exists();
    }

    @Step("Check that pending invitation is displayed")
    public boolean hasPendingInvitation() {
        waitForTable();
        return rows().stream().anyMatch(row -> row.getText().contains("Pending invitation"));
    }

    private void waitForTable() {
        TABLE.should(exist);
    }

    @Step("Check that users table is empty")
    public UsersTable shouldBeEmptyFriendsList() {
        $(byText(FRIENDS_NOT_YET_TEXT))
                .should(visible);
        return this;
    }

    private ElementsCollection rows() {
        return TABLE.$("tbody").$$("tr");
    }

}


