package guru.qa.niffler.ui.tests;

import guru.qa.niffler.api.model.UserJson;
import guru.qa.niffler.common.jupiter.annotation.User;
import guru.qa.niffler.common.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.ui.pages.AuthPage;
import guru.qa.niffler.ui.pages.FriendsPage;
import guru.qa.niffler.ui.pages.MainPage;
import guru.qa.niffler.ui.pages.PeoplePage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static guru.qa.niffler.common.jupiter.annotation.User.UserType.*;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@ExtendWith(UsersQueueExtension.class)
public class FriendsTest extends BaseUiTest {
    MainPage main = new MainPage();

    @Test
    @DisplayName("Display confirmed friends")
    void shouldDisplayConfirmedFriends(@User(WITH_FRIENDS) UserJson user) {
        step("Login as " + user.username(), () -> {
            main.open().clickLoginButton()
                    .getPage(AuthPage.class)
                    .loginAs(user.username(), user.testData().password())
                    .clickLoginButton();
        });
        FriendsPage friendsPage = main.getPage(FriendsPage.class);
        step("Open Friends page", friendsPage::open);
        step("Verify friendship is confirmed", () -> {
            assertTrue(friendsPage.hasConfirmedFriendship(), "Friendship should be confirmed");
        });
    }

    @Test
    @DisplayName("Display incoming invitation")
    void shouldDisplayIncomingInvitation(@User(INCOMING_INVITE) UserJson user) {
        step("Login as " + user.username(), () -> {
            main.open().clickLoginButton()
                    .getPage(AuthPage.class)
                    .loginAs(user.username(), user.testData().password())
                    .clickLoginButton();
        });
        FriendsPage friendsPage = main.getPage(FriendsPage.class);
        step("Open Friends page", friendsPage::open);
        step("Verify incoming invitation is displayed", () -> {
            assertTrue(friendsPage.hasIncomingInvitation(), "Incoming invitation should be displayed");
        });
    }

    @Test
    @DisplayName("Display pending invitation")
    void shouldDisplayPendingInvitation(@User(PENDING) UserJson user) {
        step("Login as " + user.username(), () -> {
            main.open().clickLoginButton()
                    .getPage(AuthPage.class)
                    .loginAs(user.username(), user.testData().password())
                    .clickLoginButton();
        });
        PeoplePage peoplePage = main.getPage(PeoplePage.class);
        step("Open People page", peoplePage::open);
        step("Verify pending invitation is displayed", () -> {
            assertTrue(peoplePage.hasPendingInvitation(), "Pending invitation should be displayed");
        });
    }

}
