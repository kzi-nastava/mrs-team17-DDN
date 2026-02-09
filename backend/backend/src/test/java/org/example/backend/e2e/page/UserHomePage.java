package org.example.backend.e2e.page;

import org.openqa.selenium.WebDriver;

public class UserHomePage extends BasePage {

    public UserHomePage(WebDriver driver) {
        super(driver);
    }

    public void waitLoaded() {
        waitForUrlContains("/user/home");
    }

    public void waitForPendingRateBanner() {
        waitVisible("pending-rate-banner");
        waitVisible("pending-rate-link");
    }

    public boolean hasPendingRateBanner() {
        return isPresent("pending-rate-banner");
    }

    public void clickPendingRateNow() {
        click("pending-rate-link");
    }
}
