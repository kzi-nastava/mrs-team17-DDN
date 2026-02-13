package org.example.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FavouriteRideDetailsPage extends BasePage {

    private static final By PAGE_ROOT = By.cssSelector(".details-page");
    private static final By START_LINE = By.cssSelector(".route-lines .line:first-of-type");
    private static final By END_LINE = By.cssSelector(".route-lines .line:last-of-type");
    private static final By CHECKPOINT_LINES = By.cssSelector(".route-lines .line-checkpoint");
    private static final By ORDER_AGAIN_BUTTON = By.cssSelector(".order-again-btn");
    private static final By ERROR_BOX = By.xpath("""
            //div[contains(@class,'details-page')]//div[contains(@class,'left')]//div[
              contains(normalize-space(), 'Favorite route not found')
              or contains(normalize-space(), 'Failed to load favourite ride details.')
              or contains(normalize-space(), 'Route is not ready for ordering')
              or contains(normalize-space(), 'Backend endpoint currently returns favorites without coordinates')
            ]
            """);

    public FavouriteRideDetailsPage(WebDriver driver) {
        super(driver);
    }

    public void waitLoaded(long favoriteRouteId) {
        waitForUrlContains("/user/favourite-rides/" + favoriteRouteId);
        waitVisible(PAGE_ROOT);
    }

    public void waitForRouteData(String expectedStartAddress, String expectedEndAddress) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(START_LINE, expectedStartAddress));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(END_LINE, expectedEndAddress));
    }

    public String startLineText() {
        return text(START_LINE);
    }

    public String endLineText() {
        return text(END_LINE);
    }

    public int checkpointCount() {
        return driver.findElements(CHECKPOINT_LINES).size();
    }

    public void clickOrderAgain() {
        click(ORDER_AGAIN_BUTTON);
    }

    public void waitForError() {
        waitVisible(ERROR_BOX);
    }

    public String errorText() {
        return text(ERROR_BOX);
    }
}
