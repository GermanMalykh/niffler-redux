package guru.qa.niffler.ui.pages;

import guru.qa.niffler.ui.elements.UsersTable;
import io.qameta.allure.Step;

public class PeoplePage extends BasePage<PeoplePage> {
    private final UsersTable peopleTable = new UsersTable();

    @Override
    public String url() {
        return "/people";
    }

    @Step("Pending invitation")
    public PeoplePage verifyPendingInvitation() {
        peopleTable.verifyPendingInvitation();
        return this;
    }

    @Step("Check that pending invitation is displayed")
    public boolean hasPendingInvitation() {
        return peopleTable.hasPendingInvitation();
    }

}
