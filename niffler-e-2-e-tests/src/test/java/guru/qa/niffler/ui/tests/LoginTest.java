package guru.qa.niffler.ui.tests;

import guru.qa.niffler.common.jupiter.annotation.DbUser;

import guru.qa.niffler.db.model.UserAuthEntity;
import guru.qa.niffler.ui.pages.AuthPage;
import guru.qa.niffler.ui.pages.MainPage;

import org.junit.jupiter.api.Test;

public class LoginTest extends BaseUiTest {
    MainPage main = new MainPage();

    @Test
    @DbUser(username = "valentin22", password = "pass22")
    void statisticShouldBeVisibleAfterLoginWithSpecifiedUser(UserAuthEntity userAuth) {
        main.open()
                .clickLoginButton()
                .getPage(AuthPage.class)
                .loginAs(userAuth.getUsername(), userAuth.getPassword())
                .getPage(MainPage.class)
                .shouldHaveSectionStats();
    }

    @Test
    @DbUser()
    void statisticShouldBeVisibleAfterLoginWithRandomUser(UserAuthEntity userAuth) {
        main.open()
                .clickLoginButton()
                .getPage(AuthPage.class)
                .loginAs(userAuth.getUsername(), userAuth.getPassword())
                .getPage(MainPage.class)
                .shouldHaveSectionStats();
    }
}
