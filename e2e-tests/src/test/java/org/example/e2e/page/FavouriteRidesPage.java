package org.example.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FavouriteRidesPage extends BasePage {

    private static final By PAGE_TITLE = By.cssSelector(".fav-page .title");
    private static final By RIDE_ROWS = By.cssSelector(".fav-page .table .row");
    private static final By DETAILS_BUTTON = By.cssSelector(".fav-page .table .row .details-btn");
    private static final By EMPTY_STATE = By.xpath(
            "//div[contains(@class,'fav-page')]//div[contains(@class,'state') and normalize-space()='No favourite routes yet.']"
    );

    public FavouriteRidesPage(WebDriver driver) {
        super(driver);
    }

    public void waitLoaded() {
        waitForUrlContains("/user/favourite-rides");
        waitVisible(PAGE_TITLE);
    }

    public void waitForEmptyState() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMPTY_STATE));
    }

    public boolean hasRows() {
        return !driver.findElements(RIDE_ROWS).isEmpty();
    }

    public boolean hasDetailsButtons() {
        return !driver.findElements(DETAILS_BUTTON).isEmpty();
    }

    public String emptyStateText() {
        return text(EMPTY_STATE);
    }

    public void clickFirstDetails() {
        click(DETAILS_BUTTON);
    }
}
