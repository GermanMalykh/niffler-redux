package guru.qa.niffler.ui.pages;

import guru.qa.niffler.ui.elements.UsersTable;
import io.qameta.allure.Step;

public class FriendsPage extends BasePage<FriendsPage> {
    private final UsersTable friendsTable = new UsersTable();

    @Override
    public String url() {
        return "/friends";
    }

    @Step("Friendship confirmed")
    public FriendsPage verifyFriendshipConfirmed() {
        friendsTable.verifyFriendshipConfirmed();
        return this;
    }

    @Step("Incoming invitation displayed")
    public FriendsPage verifyIncomingInvitation() {
        friendsTable.verifyIncomingInvitation();
        return this;
    }

    @Step("Check that friendship is confirmed")
    public boolean hasConfirmedFriendship() {
        return friendsTable.hasConfirmedFriendship();
    }

    @Step("Check that incoming invitation is displayed")
    public boolean hasIncomingInvitation() {
        return friendsTable.hasIncomingInvitation();
    }

    @Step("Check that friends table is empty")
    public FriendsPage shouldHaveNoFriends() {
        friendsTable.shouldBeEmptyFriendsList();
        return this;
    }

}