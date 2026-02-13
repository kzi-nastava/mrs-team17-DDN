package org.example.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrderRidePage extends BasePage {

    private static final By PAGE_ROOT = By.cssSelector(".order-page");
    private static final By FROM_INPUT = By.cssSelector("input[name='fromAddress']");
    private static final By TO_INPUT = By.cssSelector("input[name='toAddress']");
    private static final By CHECKPOINT_ITEMS = By.cssSelector(".checkpoint-item");
    private static final By SUBMIT_BUTTON = By.cssSelector("button.primary-action-driver[type='submit']");
    private static final By SUCCESS_BOX = By.xpath(
            "//div[contains(@class,'order-card')]//div[contains(normalize-space(),'Order placed.')]"
    );
    private static final By ERROR_BOX = By.xpath("""
            //div[contains(@class,'order-card')]//div[
              contains(normalize-space(), 'Request failed')
              or contains(normalize-space(), 'No available drivers')
              or contains(normalize-space(), 'already has an active ride')
              or contains(normalize-space(), 'blocked')
              or contains(normalize-space(), 'must be')
              or contains(normalize-space(), 'Please enter')
              or contains(normalize-space(), 'Please select')
            ]
            """);

    public OrderRidePage(WebDriver driver) {
        super(driver);
    }

    public void waitLoaded() {
        waitForUrlContains("/user/order-ride");
        waitVisible(PAGE_ROOT);
        waitVisible(FROM_INPUT);
        waitVisible(TO_INPUT);
    }

    public void waitForPrefill(String expectedFrom, String expectedTo) {
        wait.until(driver -> expectedFrom.equals(fromAddressValue()));
        wait.until(driver -> expectedTo.equals(toAddressValue()));
    }

    public String fromAddressValue() {
        String value = waitVisible(FROM_INPUT).getAttribute("value");
        return value == null ? "" : value.trim();
    }

    public String toAddressValue() {
        String value = waitVisible(TO_INPUT).getAttribute("value");
        return value == null ? "" : value.trim();
    }

    public int checkpointCount() {
        return driver.findElements(CHECKPOINT_ITEMS).size();
    }

    public void submit() {
        click(SUBMIT_BUTTON);
    }

    public void waitForOutcome() {
        wait.until(driver -> hasSuccessMessage() || hasErrorMessage());
    }

    public boolean hasSuccessMessage() {
        return !driver.findElements(SUCCESS_BOX).isEmpty();
    }

    public boolean hasErrorMessage() {
        return !driver.findElements(ERROR_BOX).isEmpty();
    }

    public void waitForSuccess() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_BOX));
    }

    public String successText() {
        return text(SUCCESS_BOX);
    }

    public void waitForError() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_BOX));
    }

    public String errorText() {
        if (!hasErrorMessage()) {
            return "";
        }
        return text(ERROR_BOX);
    }
}
