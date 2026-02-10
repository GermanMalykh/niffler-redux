package guru.qa.niffler.ui.pages;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;

public abstract class BasePage<T extends BasePage> {
    public abstract String url();

    @Step("Open page")
    public T open() {
        String pageUrl = url();
        return Selenide.open(pageUrl, (Class<T>) this.getClass());
    }

    @Step("Get page")
    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }
}
